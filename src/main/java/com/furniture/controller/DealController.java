package com.furniture.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.modal.Deal;
import com.furniture.response.ApiResponse;
import com.furniture.service.DealService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    // ========== PUBLIC ENDPOINTS ==========
    
    @GetMapping("/api/deals")
    public ResponseEntity<List<Deal>> getDeals() {
        List<Deal> deals = dealService.getDeals();
        return ResponseEntity.ok(deals);
    }

    // ========== ADMIN ENDPOINTS ==========

    @GetMapping("/admin/deals")
    public ResponseEntity<List<Deal>> getAllDealsAdmin() {
        List<Deal> deals = dealService.getDeals();
        return ResponseEntity.ok(deals);
    }

    @PostMapping("/admin/deals")
    public ResponseEntity<Deal> createDeals(
            @RequestBody Deal deal
    ) {
        Deal createdDeal = dealService.createDeal(deal);
        return new ResponseEntity<>(createdDeal, HttpStatus.ACCEPTED);
    }

    @PatchMapping("/admin/deals/{id}")
    public ResponseEntity<Deal> updateDeal(
            @PathVariable Long id,
            @RequestBody Deal deal
    ) throws Exception {

        Deal updatedDeal = dealService.updateDeal(deal, id);
        return ResponseEntity.ok(updatedDeal);
    }

    @DeleteMapping("/admin/deals/{id}")
    public ResponseEntity<ApiResponse> deleteDeal(@PathVariable Long id) throws Exception {
        dealService.deleteDeal(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Deal deleted successfully");

        return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
    }
}
