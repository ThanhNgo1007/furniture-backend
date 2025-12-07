package com.furniture.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.furniture.exceptions.ProductException;
import com.furniture.modal.Product;
import com.furniture.modal.Seller;
import com.furniture.request.CreateProductRequest;

public interface ProductService {

    public Product createProduct(CreateProductRequest req, Seller seller);
    public void deleteProduct(Long productId) throws ProductException;
    public Product updateProduct(Long productId, Product product) throws ProductException;
    Product findProductById(Long productId) throws ProductException;
    List<Product> searchProducts(String query);
    public Page<Product> getAllProducts(
            String category,
            String brand,
            String colors,
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            String sort,
            String stock,
            Integer pageNumber
    );
    List<Product> getProductBySellerId(Long sellerId);
    
    Product softDeleteProduct(Long productId) throws ProductException;
    
    Product markOutOfStock(Long productId) throws ProductException;
    
    Product reactivateProduct(Long productId) throws ProductException;
    
    List<Product> getInactiveProductsBySellerId(Long sellerId);

}
