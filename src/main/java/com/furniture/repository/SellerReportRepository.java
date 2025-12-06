package com.furniture.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.modal.SellerReport;

public interface SellerReportRepository extends JpaRepository<SellerReport, Long> {

    SellerReport findBySellerId(Long sellerId);
}
