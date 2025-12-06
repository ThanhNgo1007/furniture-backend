package com.furniture.controller;

import com.furniture.modal.Seller;
import com.furniture.service.SellerDashboardService;
import com.furniture.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final SellerService sellerService;
    private final SellerDashboardService sellerDashboardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData(@RequestHeader("Authorization") String jwt) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);
        Map<String, Object> data = sellerDashboardService.getSellerDashboardData(seller.getId());
        return ResponseEntity.ok(data);
    }
}
