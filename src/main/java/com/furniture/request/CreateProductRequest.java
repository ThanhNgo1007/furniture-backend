package com.furniture.request;

import com.furniture.modal.Category;
import lombok.Data;

import java.util.List;

@Data
public class CreateProductRequest {

    private String title;
    private String description;
    private int msrpPrice;
    private int sellingPrice;
    private int quantity;
    private String color;
    private String room;
    private List<String> images;
    private String category;
    private String category2;
    private String category3;
}
