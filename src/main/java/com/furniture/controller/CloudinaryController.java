package com.furniture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.service.CloudinaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {
    
    private final CloudinaryService cloudinaryService;
    
    /**
     * Delete an image from Cloudinary
     * @param imageUrl The full Cloudinary URL of the image to delete
     * @return Success or error response
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestParam String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Image URL is required");
        }
        
        boolean deleted = cloudinaryService.deleteImage(imageUrl);
        
        if (deleted) {
            return ResponseEntity.ok("Image deleted successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to delete image");
        }
    }
}
