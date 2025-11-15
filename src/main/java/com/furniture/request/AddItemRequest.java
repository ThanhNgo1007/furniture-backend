package com.furniture.request;

import lombok.Data;

@Data
public class AddItemRequest {
    private int quantity;
    private Long productId;
}
