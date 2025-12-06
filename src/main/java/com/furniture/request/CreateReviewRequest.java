package com.furniture.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 500, message = "Review must be between 10 and 500 characters")
    private String reviewText;
    
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private double reviewRating;
    
    @Size(max = 3, message = "Maximum 3 images allowed")
    private List<String> productImages;

    private Long orderId;
}
