package com.furniture.request;

import com.furniture.modal.Category;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {

    private String title;
    private String description;
    private BigDecimal msrpPrice;
    private BigDecimal sellingPrice;
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    private String color;
    private String room;
    private List<String> images;
    private String category;
    private String category2;
    private String category3;
}
