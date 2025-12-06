package com.furniture.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.domain.OrderStatus;
import com.furniture.modal.Seller;
import com.furniture.modal.SellerReport;
import com.furniture.repository.OrderItemRepository;
import com.furniture.repository.OrderRepository;
import com.furniture.repository.SellerReportRepository;
import com.furniture.repository.SellerRepository;
import com.furniture.service.SellerReportService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerReportServiceImpl implements SellerReportService {

    private final SellerReportRepository sellerReportRepository;
    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Get seller report - Reads from seller_report table
     * This table is updated every 5 minutes by batch job
     */
    @Override
    public SellerReport getSellerReport(Seller seller) {
        SellerReport report = sellerReportRepository.findBySellerId(seller.getId());

        if (report == null) {
            // Create initial report if not exists
            report = new SellerReport();
            report.setSeller(seller);
            return sellerReportRepository.save(report);
        }
        
        return report;
    }

    /**
     * @deprecated This method should not be called directly anymore.
     * Reports are updated via scheduled batch job.
     */
    @Override
    @Deprecated
    public SellerReport updateSellerReport(@NonNull SellerReport sellerReport) {
        log.warn("⚠️ Direct update of SellerReport is deprecated. Use batch job instead.");
        return sellerReportRepository.save(sellerReport);
    }

    /**
     * BATCH UPDATE JOB
     * Runs every 5 minutes to recalculate all seller reports
     * Cron: "0 * /5 * * * ?" = At second 0 of every 5th minute
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void batchUpdateAllSellerReports() {
        log.info("🔄 Starting batch update of seller reports...");
        
        long startTime = System.currentTimeMillis();
        List<Seller> sellers = sellerRepository.findAll();
        int updatedCount = 0;

        for (Seller seller : sellers) {
            try {
                updateSingleSellerReport(seller);
                updatedCount++;
            } catch (Exception e) {
                log.error("❌ Failed to update report for seller {}: {}", 
                         seller.getId(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Batch update completed: {} sellers updated in {}ms", 
                 updatedCount, duration);
    }

    /**
     * Calculate and update report for a single seller
     */
    private void updateSingleSellerReport(Seller seller) {
        Long sellerId = seller.getId();
        
        // 1. Aggregate data from orders table
        Long totalOrders = orderRepository.countTotalOrdersBySellerId(sellerId);
        
        BigDecimal totalEarnings = orderRepository.sumEarningsBySellerId(sellerId);
        if (totalEarnings == null) totalEarnings = BigDecimal.ZERO;
        
        Long totalSales = orderItemRepository.sumQuantityBySellerIdForDeliveredOrders(sellerId);
        if (totalSales == null) totalSales = 0L;
        
        BigDecimal totalRefunds = orderRepository.sumRefundsBySellerId(sellerId);
        if (totalRefunds == null) totalRefunds = BigDecimal.ZERO;
        
        Long canceledOrders = orderRepository.countBySellerIdAndOrderStatus(
            sellerId, OrderStatus.CANCELLED
        );
        
        // 2. Get or create report
        SellerReport report = sellerReportRepository.findBySellerId(sellerId);
        if (report == null) {
            report = new SellerReport();
            report.setSeller(seller);
        }
        
        // 3. Update all fields
        report.setTotalOrders(totalOrders.intValue());
        report.setTotalEarnings(totalEarnings);
        report.setTotalSales(totalSales.intValue());
        report.setTotalRefunds(totalRefunds);
        report.setCanceledOrders(canceledOrders.intValue());
        
        // ✅ Calculate platform fee (5% commission)
        BigDecimal platformFee = totalEarnings.multiply(new BigDecimal("0.05"));
        report.setPlatformFee(platformFee);
        
        // ✅ Calculate net earnings (after platform fee)
        // Net Earnings = Total Earnings - Platform Fee
        BigDecimal netEarnings = totalEarnings.subtract(platformFee);
        report.setNetEarnings(netEarnings);
        
        // ✅ Set last updated timestamp
        report.setLastUpdated(LocalDateTime.now());
        
        // 4. Save to database
        sellerReportRepository.save(report);
        
        log.debug("📊 Updated report for seller {}: {} orders, {} earnings", 
                 sellerId, totalOrders, totalEarnings);
    }

    /**
     * Manual trigger for updating a specific seller's report
     * Useful for testing or admin operations
     */
    @Transactional
    public void updateSellerReportNow(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Seller not found: " + sellerId));
        
        updateSingleSellerReport(seller);
        log.info("✅ Manually updated report for seller {}", sellerId);
    }
}
