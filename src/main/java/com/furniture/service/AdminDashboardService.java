package com.furniture.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.furniture.domain.AccountStatus;
import com.furniture.domain.OrderStatus;
import com.furniture.domain.PaymentMethod;
import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Order;
import com.furniture.repository.OrderRepository;
import com.furniture.repository.ProductRepository;
import com.furniture.repository.SellerRepository;
import com.furniture.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;

    /**
     * Get complete admin dashboard data
     */
    public Map<String, Object> getAdminDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();

        // 1. Summary Stats
        dashboard.put("summary", getSummaryStats());

        // 2. Order Statistics
        dashboard.put("orderStats", getOrderStats());

        // 3. Daily Revenue (30 days)
        dashboard.put("dailyRevenue", getDailyRevenue());
        
        // 4. Daily Orders (30 days)
        dashboard.put("dailyOrders", getDailyOrders());

        // 5. Top Sellers
        dashboard.put("topSellers", getTopSellers());

        // 6. Payment Breakdown
        dashboard.put("paymentBreakdown", getPaymentBreakdown());

        // 7. User Activity
        dashboard.put("userActivity", getUserActivity());

        return dashboard;
    }

    /**
     * Tab 1: Summary Overview
     */
    private Map<String, Object> getSummaryStats() {
        Map<String, Object> summary = new HashMap<>();

        // Total Revenue (GMV) - from DELIVERED orders
        BigDecimal totalRevenue = orderRepository.sumTotalRevenueByStatus(OrderStatus.DELIVERED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        summary.put("totalRevenue", totalRevenue);

        // Platform Commission (5%)
        BigDecimal platformCommission = totalRevenue.multiply(new BigDecimal("0.05"));
        summary.put("platformCommission", platformCommission);

        // Total Orders (all statuses)
        long totalOrders = orderRepository.count();
        summary.put("totalOrders", totalOrders);

        // Total Customers
        long totalCustomers = userRepository.countByRole(USER_ROLE.ROLE_CUSTOMER);
        summary.put("totalCustomers", totalCustomers);

        // Total Sellers (active)
        long totalSellers = sellerRepository.countByAccountStatus(AccountStatus.ACTIVE);
        summary.put("totalSellers", totalSellers);

        // Total Products
        long totalProducts = productRepository.count();
        summary.put("totalProducts", totalProducts);

        // Today's stats
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        long ordersToday = orderRepository.countByOrderDateBetween(todayStart, todayEnd);
        summary.put("ordersToday", ordersToday);

        BigDecimal revenueToday = orderRepository.sumRevenueByDateRangeAndStatus(
            todayStart, todayEnd, OrderStatus.DELIVERED
        );
        if (revenueToday == null) revenueToday = BigDecimal.ZERO;
        summary.put("revenueToday", revenueToday);

        return summary;
    }

    /**
     * Tab 2: Order Statistics
     */
    private Map<String, Object> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();

        // Count by status
        long pending = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long confirmed = orderRepository.countByOrderStatus(OrderStatus.CONFIRMED);
        long shipped = orderRepository.countByOrderStatus(OrderStatus.SHIPPED);
        long delivered = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelled = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);

        stats.put("pending", pending);
        stats.put("confirmed", confirmed);
        stats.put("shipped", shipped);
        stats.put("delivered", delivered);
        stats.put("cancelled", cancelled);

        // Total orders (excluding pending)
        long totalProcessed = confirmed + shipped + delivered + cancelled;
        stats.put("totalProcessed", totalProcessed);

        // Cancel Rate
        double cancelRate = totalProcessed > 0 
            ? (cancelled * 100.0 / totalProcessed) 
            : 0;
        stats.put("cancelRate", Math.round(cancelRate * 100.0) / 100.0);

        // Success Rate
        double successRate = totalProcessed > 0 
            ? (delivered * 100.0 / totalProcessed) 
            : 0;
        stats.put("successRate", Math.round(successRate * 100.0) / 100.0);

        // Average Order Value (AOV)
        BigDecimal avgOrderValue = orderRepository.avgOrderValueByStatus(OrderStatus.DELIVERED);
        if (avgOrderValue == null) avgOrderValue = BigDecimal.ZERO;
        stats.put("avgOrderValue", avgOrderValue.setScale(0, RoundingMode.HALF_UP));

        return stats;
    }

    /**
     * Daily Revenue for last 30 days
     */
    private Map<String, BigDecimal> getDailyRevenue() {
        int days = 30;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Order> orders = orderRepository.findByDeliveryDateBetweenAndOrderStatus(
            startDate, endDate, OrderStatus.DELIVERED
        );

        Map<String, BigDecimal> revenueByDate = new TreeMap<>();
        
        // Initialize all days with 0
        for (int i = 0; i <= days; i++) {
            LocalDate date = startDate.plusDays(i).toLocalDate();
            revenueByDate.put(date.toString(), BigDecimal.ZERO);
        }

        // Sum revenue by date
        for (Order order : orders) {
            if (order.getDeliveryDate() != null) {
                String dateKey = order.getDeliveryDate().toLocalDate().toString();
                BigDecimal current = revenueByDate.getOrDefault(dateKey, BigDecimal.ZERO);
                revenueByDate.put(dateKey, current.add(order.getTotalSellingPrice()));
            }
        }

        return revenueByDate;
    }

    /**
     * Daily Orders count for last 30 days
     */
    private Map<String, Long> getDailyOrders() {
        int days = 30;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);

        Map<String, Long> ordersByDate = new TreeMap<>();
        
        // Initialize all days with 0
        for (int i = 0; i <= days; i++) {
            LocalDate date = startDate.plusDays(i).toLocalDate();
            ordersByDate.put(date.toString(), 0L);
        }

        // Count orders by date
        for (Order order : orders) {
            if (order.getOrderDate() != null) {
                String dateKey = order.getOrderDate().toLocalDate().toString();
                Long current = ordersByDate.getOrDefault(dateKey, 0L);
                ordersByDate.put(dateKey, current + 1);
            }
        }

        return ordersByDate;
    }

    /**
     * Tab 3: Top 5 Sellers by Revenue
     */
    private List<Map<String, Object>> getTopSellers() {
        List<Object[]> topSellersData = orderRepository.findTopSellersByRevenue(5);
        
        List<Map<String, Object>> topSellers = new ArrayList<>();
        int rank = 1;
        
        for (Object[] row : topSellersData) {
            Map<String, Object> seller = new HashMap<>();
            seller.put("rank", rank++);
            seller.put("sellerId", row[0]);
            seller.put("sellerName", row[1] != null ? row[1] : "Unknown");
            seller.put("revenue", row[2] != null ? row[2] : BigDecimal.ZERO);
            seller.put("orderCount", row[3] != null ? row[3] : 0L);
            topSellers.add(seller);
        }

        return topSellers;
    }

    /**
     * Tab 4: Payment Method Breakdown
     */
    private Map<String, Object> getPaymentBreakdown() {
        Map<String, Object> breakdown = new HashMap<>();

        // VNPay stats
        Map<String, Object> vnpay = new HashMap<>();
        long vnpayCount = orderRepository.countByPaymentMethod(PaymentMethod.VNPAY);
        BigDecimal vnpayAmount = orderRepository.sumRevenueByPaymentMethod(PaymentMethod.VNPAY);
        if (vnpayAmount == null) vnpayAmount = BigDecimal.ZERO;
        vnpay.put("count", vnpayCount);
        vnpay.put("amount", vnpayAmount);
        breakdown.put("vnpay", vnpay);

        // COD stats
        Map<String, Object> cod = new HashMap<>();
        long codCount = orderRepository.countByPaymentMethod(PaymentMethod.COD);
        BigDecimal codAmount = orderRepository.sumRevenueByPaymentMethod(PaymentMethod.COD);
        if (codAmount == null) codAmount = BigDecimal.ZERO;
        cod.put("count", codCount);
        cod.put("amount", codAmount);
        breakdown.put("cod", cod);

        // Calculate percentages
        long total = vnpayCount + codCount;
        if (total > 0) {
            breakdown.put("vnpayPercent", Math.round(vnpayCount * 100.0 / total));
            breakdown.put("codPercent", Math.round(codCount * 100.0 / total));
        } else {
            breakdown.put("vnpayPercent", 0);
            breakdown.put("codPercent", 0);
        }

        return breakdown;
    }

    /**
     * User Activity Stats
     */
    private Map<String, Object> getUserActivity() {
        Map<String, Object> activity = new HashMap<>();

        // Pending sellers (need approval)
        long pendingSellers = sellerRepository.countByAccountStatus(AccountStatus.PENDING_VERIFICATION);
        activity.put("pendingSellers", pendingSellers);

        // Note: These would require createdAt field on User/Seller entities
        // For now, return placeholders
        activity.put("newCustomersThisMonth", 0);
        activity.put("newSellersThisMonth", 0);

        return activity;
    }
}
