package com.furniture.modal;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal; // Nhớ import BigDecimal

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

    // --- THAY ĐỔI TỪ ĐÂY ---

    // Đổi Long -> BigDecimal và khởi tạo bằng ZERO
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    private Integer totalSales = 0;

    private BigDecimal totalRefunds = BigDecimal.ZERO;

    private BigDecimal totalTax = BigDecimal.ZERO;

    private BigDecimal netEarnings = BigDecimal.ZERO;

    // Các trường số lượng giữ nguyên Integer
    private Integer totalOrders = 0;

    private Integer canceledOrders = 0;

    private Integer totalTransactions = 0;
}