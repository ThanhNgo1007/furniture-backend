package com.furniture.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CloudinaryService {
    
    @Value("${cloudinary.cloud-name}")
    private String cloudName;
    
    @Value("${cloudinary.api-key}")
    private String apiKey;
    
    @Value("${cloudinary.api-secret}")
    private String apiSecret;
    
    private Cloudinary cloudinary;
    
    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
        log.info("Cloudinary initialized with cloud_name: {}", cloudName);
    }
    
    /**
     * Extract public_id from Cloudinary URL
     * Example URL: https://res.cloudinary.com/dtlxpw3eh/image/upload/v1234567890/folder/image_name.jpg
     * Returns: folder/image_name (without extension)
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        try {
            // Find the "/upload/" part and extract everything after it
            String uploadMarker = "/upload/";
            int uploadIndex = imageUrl.indexOf(uploadMarker);
            
            if (uploadIndex == -1) {
                log.warn("Invalid Cloudinary URL format: {}", imageUrl);
                return null;
            }
            
            // Get the part after "/upload/"
            String afterUpload = imageUrl.substring(uploadIndex + uploadMarker.length());
            
            // Skip version number if present (e.g., "v1234567890/")
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                int slashIndex = afterUpload.indexOf("/");
                afterUpload = afterUpload.substring(slashIndex + 1);
            }
            
            // Remove file extension
            int lastDotIndex = afterUpload.lastIndexOf(".");
            if (lastDotIndex != -1) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }
            
            return afterUpload;
        } catch (Exception e) {
            log.error("Error extracting public_id from URL: {}", imageUrl, e);
            return null;
        }
    }
    
    /**
     * Delete an image from Cloudinary by its URL
     * @param imageUrl The full Cloudinary URL of the image
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteImage(String imageUrl) {
        String publicId = extractPublicId(imageUrl);
        
        if (publicId == null) {
            log.error("Could not extract public_id from URL: {}", imageUrl);
            return false;
        }
        
        return deleteImageByPublicId(publicId);
    }
    
    /**
     * Delete an image from Cloudinary by its public_id
     * @param publicId The public_id of the image
     * @return true if deletion was successful, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean deleteImageByPublicId(String publicId) {
        try {
            log.info("Deleting image with public_id: {}", publicId);
            
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            String resultStatus = (String) result.get("result");
            
            if ("ok".equals(resultStatus)) {
                log.info("Successfully deleted image: {}", publicId);
                return true;
            } else if ("not found".equals(resultStatus)) {
                log.warn("Image not found on Cloudinary: {}", publicId);
                return true; // Consider as success since image doesn't exist
            } else {
                log.error("Failed to delete image: {} - Result: {}", publicId, result);
                return false;
            }
        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", publicId, e);
            return false;
        }
    }
}
