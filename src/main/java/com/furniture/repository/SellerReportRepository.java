package com.furniture.repository;

import com.furniture.modal.Seller;
import com.furniture.modal.SellerReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerReportRepository extends JpaRepository<SellerReport, Long> {

    SellerReport findBySellerId(Long sellerId);
}
