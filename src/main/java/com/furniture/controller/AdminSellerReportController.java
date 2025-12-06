package com.furniture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.response.ApiResponse;
import com.furniture.service.impl.SellerReportServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/seller-reports")
public class AdminSellerReportController {
    
    private final SellerReportServiceImpl sellerReportService;
    
    /**
     * Manually trigger batch update for all sellers
     * Useful for: Testing, fixing data issues, or forcing immediate update
     */
    @PostMapping("/batch-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> triggerBatchUpdate() {
        sellerReportService.batchUpdateAllSellerReports();
        
        ApiResponse response = new ApiResponse();
        response.setMessage("Batch update triggered successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Manually update a specific seller's report
     */
    @PostMapping("/{sellerId}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateSellerReport(@PathVariable Long sellerId) {
        sellerReportService.updateSellerReportNow(sellerId);
        
        ApiResponse response = new ApiResponse();
        response.setMessage("Seller report updated successfully for seller: " + sellerId);
        return ResponseEntity.ok(response);
    }
}
