package com.furniture.service.impl;

import com.furniture.exceptions.ProductException;
import com.furniture.modal.Category;
import com.furniture.modal.Product;
import com.furniture.modal.Seller;
import com.furniture.repository.CategoryRepository;
import com.furniture.repository.ProductRepository;
import com.furniture.request.CreateProductRequest;
import com.furniture.service.ProductService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Product createProduct(CreateProductRequest req, Seller seller) {

        Category category1 = categoryRepository.findByCategoryId(req.getCategory());

        if (category1 == null) {
            Category category = new Category();
            category.setCategoryId(req.getCategory());
            category.setLevel(1);
            category1 = categoryRepository.save(category);
        }

        Category category2 = categoryRepository.findByCategoryId(req.getCategory2());

        if (category2 == null) {
            Category category = new Category();
            category.setCategoryId(req.getCategory2());
            category.setLevel(2);
            category.setParentCategory(category1);
            category2 = categoryRepository.save(category);
        }

        Category category3 = categoryRepository.findByCategoryId(req.getCategory3());

        if (category3 == null) {
            Category category = new Category();
            category.setCategoryId(req.getCategory3());
            category.setLevel(3);
            category.setParentCategory(category2);
            category3 = categoryRepository.save(category);
        }

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category3);
        product.setDescription(req.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());
        product.setSellingPrice(req.getSellingPrice());
        product.setImages(req.getImages());
        product.setMsrpPrice(req.getMsrpPrice());
        product.setQuantity(req.getQuantity());
        product.setDiscountPercent(calculateDiscountPercentage(req.getMsrpPrice(), req.getSellingPrice()));

        return productRepository.save(product);

    }


    private int calculateDiscountPercentage(BigDecimal msrpPrice, BigDecimal sellingPrice) {
        if (msrpPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        // (msrp - selling)
        BigDecimal discount = msrpPrice.subtract(sellingPrice);

        // (discount / msrp) * 100 -> Cần set scale và RoundingMode để tránh lỗi chia số lẻ vô hạn
        return discount.divide(msrpPrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }


    @Override
    public void deleteProduct(Long productId) throws ProductException {

        Product product = findProductById(productId);
        productRepository.delete(product);

    }

    @Override
    public Product updateProduct(Long productId, Product product) throws ProductException {
        findProductById(productId);
        product.setId(productId);

        return productRepository.save(product);
    }

    @Override
    public Product findProductById(Long productId) throws ProductException {
        return productRepository.findById(productId).orElseThrow(() ->
                new ProductException("product not found with id"+productId));
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productRepository.searchProduct(query);
    }

    @Override
    public Page<Product> getAllProducts(String category, String brand, String colors, Integer minPrice, Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber) {

        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null && !category.isEmpty()) {
                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("categoryId"), category));
            }

            // --- SỬA LẠI LOGIC LỌC MÀU ---
            if (colors != null && !colors.isEmpty()) {
                // Dùng lower() và like %...% để tìm kiếm linh hoạt
                // Ví dụ: Tìm "white" sẽ ra cả "White", "Off White"
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("color")),
                        "%" + colors.toLowerCase() + "%"
                ));
            }

            // --- CHỈ LỌC GIÁ KHI KHÁC NULL ---
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }

            if (minDiscount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountPercent"), minDiscount));
            }

            if (stock != null) {
                predicates.add(criteriaBuilder.equal(root.get("stock"), stock));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // --- LOGIC SORT & PAGINATION ---
        Pageable pageable;

        // Xác định pageSize (Frontend gửi lên hoặc mặc định 10)
        // Lưu ý: Trong interface Service bạn chưa có tham số pageSize,
        // nếu chưa sửa interface thì hardcode logic ở đây:
        int pageSize = 8;

        if (sort != null && !sort.isEmpty()) {
            pageable = switch (sort) {
                case "price_low" -> PageRequest.of(pageNumber, pageSize, Sort.by("sellingPrice").ascending());
                case "price_high" -> PageRequest.of(pageNumber, pageSize, Sort.by("sellingPrice").descending());
                case "newest" -> PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

                // CASE RANDOM: Trả về unsorted (hoặc sort theo ID để ổn định)
                case "random" -> PageRequest.of(pageNumber, pageSize, Sort.unsorted());

                default -> PageRequest.of(pageNumber, pageSize, Sort.unsorted());
            };
        } else {
            // Mặc định nếu không có sort (cũng coi như random/mới vào)
            pageable = PageRequest.of(pageNumber, 8, Sort.unsorted());
        }

        return productRepository.findAll(spec, pageable);
    }

    @Override
    public List<Product> getProductBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
}
