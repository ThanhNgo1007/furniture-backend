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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
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
        product.setDiscountPercent(calculateDiscountPercentage(req.getMsrpPrice(), req.getSellingPrice()));

        return productRepository.save(product);

    }

//    @Override
//    public Product createProduct(CreateProductRequest req, Seller seller) {
//
//        // --- ROOM (Level 1 - Rooms) ---
//        Category room = categoryRepository.findByCategoryId(req.getRoom());
//        if (room == null) {
//            room = new Category();
//            room.setCategoryId(req.getRoom());
//            room.setName(req.getRoom());
//            room.setLevel(1);
//            room = categoryRepository.save(room);
//        }
//
//        // --- CATEGORY LEVEL 1 (Products) ---
//        Category cat1 = categoryRepository.findByCategoryId(req.getCategory());
//        if (cat1 == null) {
//            cat1 = new Category();
//            cat1.setCategoryId(req.getCategory());
//            cat1.setName(req.getCategory());
//            cat1.setLevel(1);
//            cat1 = categoryRepository.save(cat1);
//        }
//
//        // --- CATEGORY LEVEL 2 (Storage & organization / Bedroom) ---
//        Category cat2 = categoryRepository.findByCategoryId(req.getCategory2());
//        if (cat2 == null) {
//            cat2 = new Category();
//            cat2.setCategoryId(req.getCategory2());
//            cat2.setName(req.getCategory2());
//            cat2.setLevel(2);
//            // liên kết với Products hoặc Room
//            cat2.getParentCategory().add(cat1);
//            cat2.getParentCategory().add(room);
//            cat2 = categoryRepository.save(cat2);
//        }
//
//        // --- CATEGORY LEVEL 3 (Dressers & drawers) ---
//        Category cat3 = categoryRepository.findByCategoryId(req.getCategory3());
//        if (cat3 == null) {
//            cat3 = new Category();
//            cat3.setCategoryId(req.getCategory3());
//            cat3.setName(req.getCategory3());
//            cat3.setLevel(3);
//        }
//        // luôn đảm bảo có 2 parent: Product path & Room path
//        cat3.getParentCategory().add(cat2);
//        categoryRepository.save(cat3);
//
//        // --- TẠO PRODUCT ---
//        Product product = new Product();
//        product.setSeller(seller);
//        product.setRoom(room);
//        product.setCategories(Set.of(cat3));
//        product.setDescription(req.getDescription());
//        product.setCreatedAt(LocalDateTime.now());
//        product.setTitle(req.getTitle());
//        product.setColor(req.getColor());
//        product.setSellingPrice(req.getSellingPrice());
//        product.setMsrpPrice(req.getMsrpPrice());
//        product.setQuantity(req.getQuantity());
//        product.setImages(req.getImages());
//        product.setDiscountPercent(calculateDiscountPercentage(req.getMsrpPrice(), req.getSellingPrice()));
//
//        return productRepository.save(product);
//    }

    private int calculateDiscountPercentage(int msrpPrice, int sellingPrice) {
        if (msrpPrice <= 0) {
            throw new IllegalArgumentException("Actual price must be greater than 0");
        }
        double discount = msrpPrice - sellingPrice;
        double discountPercentage = (discount/msrpPrice) * 100.0;

        return (int)discountPercentage;
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

            if (category != null) {
                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("categoryId"), category));
            }
            if (colors != null && !colors.isEmpty()) {
                System.out.println("colors: " + colors);
                predicates.add(criteriaBuilder.like(root.get("color"), colors));
            }
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
                predicates.add(criteriaBuilder.like(root.get("stock"), stock));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable;

        if (sort != null && !sort.isEmpty()) {
            pageable = switch (sort) {
                case "price_low" -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
                        Sort.by("sellingPrice").ascending());
                case "price_high" -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
                        Sort.by("sellingPrice").descending());
                default -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
                        Sort.unsorted());
            };
        }
        else {
            pageable = PageRequest.of(pageNumber != null ? pageNumber : 0, 10, Sort.unsorted());
        }
        return productRepository.findAll(spec, pageable);
    }

    @Override
    public List<Product> getProductBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
}
