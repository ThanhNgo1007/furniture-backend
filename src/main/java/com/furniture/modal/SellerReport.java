package com.furniture.modal;

import java.math.BigDecimal; // Nhớ import BigDecimal
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SellerReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Seller seller;

    // Revenue & Earnings
    private BigDecimal totalEarnings = BigDecimal.ZERO;  // Tổng doanh thu từ đơn DELIVERED
    
    private BigDecimal platformFee = BigDecimal.ZERO;    // Phí sàn (5% của totalEarnings)
    
    private BigDecimal netEarnings = BigDecimal.ZERO;    // Thu nhập ròng (totalEarnings - platformFee)

    // Sales & Orders
    private Integer totalSales = 0;                      // Tổng số lượng sản phẩm bán được

    private Integer totalOrders = 0;                     // Tổng số đơn hàng

    private Integer canceledOrders = 0;                  // Số đơn bị hủy

    // Refunds & Tax
    private BigDecimal totalRefunds = BigDecimal.ZERO;   // Tổng tiền từ đơn CANCELLED (chỉ để thống kê)

    private BigDecimal totalTax = BigDecimal.ZERO;       // Thuế (nếu có)

    // Transactions
    private Integer totalTransactions = 0;               // Tổng số giao dịch
    
    // Metadata
    private LocalDateTime lastUpdated;                   // Thời gian cập nhật lần cuối bởi batch job
}