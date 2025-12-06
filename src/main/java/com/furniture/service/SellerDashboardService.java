package com.furniture.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.furniture.domain.OrderStatus;
import com.furniture.modal.Order;
import com.furniture.modal.Seller;
import com.furniture.modal.SellerReport;
import com.furniture.repository.OrderRepository;
import com.furniture.repository.ProductRepository;
import com.furniture.repository.SellerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SellerReportService sellerReportService;
    private final SellerRepository sellerRepository;

    public Map<String, Object> getSellerDashboardData(Long sellerId) {
        Map<String, Object> dashboardData = new HashMap<>();

        // ✅ Get seller and report (batch data - updated every 5 minutes)
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        SellerReport report = sellerReportService.getSellerReport(seller);

        // 1. Summary Stats (FROM BATCH REPORT)
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", report.getTotalOrders());
        summary.put("lastUpdated", report.getLastUpdated()); // Timestamp
        summary.put("totalRevenue", report.getTotalEarnings());
        
        // Real-time order status breakdown (for accuracy)
        long pendingOrders = orderRepository.countBySellerIdAndOrderStatus(sellerId, OrderStatus.PENDING);
        long shippingOrders = orderRepository.countBySellerIdAndOrderStatus(sellerId, OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countBySellerIdAndOrderStatus(sellerId, OrderStatus.DELIVERED);
        long cancelledOrders = report.getCanceledOrders(); // From batch
        
        summary.put("pendingOrders", pendingOrders);
        summary.put("shippingOrders", shippingOrders);
        summary.put("deliveredOrders", deliveredOrders);
        summary.put("cancelledOrders", cancelledOrders);

        dashboardData.put("summary", summary);

        // 2. Daily Revenue (Last 30 days) - Real-time for chart
        int days = 30;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Order> orders = orderRepository.findBySellerIdAndDeliveryDateBetween(sellerId, startDate, endDate);

        Map<String, BigDecimal> revenueByDate = new TreeMap<>();
        for (int i = 0; i <= days; i++) {
            LocalDate date = startDate.plusDays(i).toLocalDate();
            revenueByDate.put(date.toString(), BigDecimal.ZERO);
        }

        for (Order order : orders) {
            if (order.getDeliveryDate() != null && order.getOrderStatus() == OrderStatus.DELIVERED) {
                String dateKey = order.getDeliveryDate().toLocalDate().toString();
                BigDecimal current = revenueByDate.getOrDefault(dateKey, BigDecimal.ZERO);
                revenueByDate.put(dateKey, current.add(order.getTotalSellingPrice()));
            }
        }
        dashboardData.put("dailyRevenue", revenueByDate);

        // 3. Finance (FROM BATCH REPORT)
        Map<String, Object> finance = new HashMap<>();
        finance.put("totalRevenue", report.getTotalEarnings());
        finance.put("platformFee", report.getPlatformFee());
        finance.put("lastUpdated", report.getLastUpdated()); // Timestamp
        finance.put("netProfit", report.getNetEarnings());
        finance.put("totalDiscount", orderRepository.sumTotalDiscountBySellerId(sellerId)); // Real-time for accuracy
        dashboardData.put("finance", finance);

        // 4. Products (Real-time)
        long totalProducts = productRepository.countBySellerId(sellerId);
        long lowStockProducts = productRepository.countBySellerIdAndQuantityLessThan(sellerId, 10);

        Map<String, Object> products = new HashMap<>();
        products.put("totalProducts", totalProducts);
        products.put("lowStockProducts", lowStockProducts);
        dashboardData.put("products", products);

        // 5. Customers (Real-time)
        long totalCustomers = orderRepository.countDistinctUserBySellerId(sellerId);
        Map<String, Object> customers = new HashMap<>();
        customers.put("totalCustomers", totalCustomers);
        dashboardData.put("customers", customers);

        return dashboardData;
    }
}
