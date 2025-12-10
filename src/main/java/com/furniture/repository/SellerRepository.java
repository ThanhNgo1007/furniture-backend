package com.furniture.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.domain.AccountStatus;
import com.furniture.modal.Seller;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Seller findByEmail(String email);
    
    List<Seller> findByAccountStatus(AccountStatus status);
    Page<Seller> findByAccountStatus(
        AccountStatus status, Pageable pageable);
    
    // Count sellers by account status
    long countByAccountStatus(AccountStatus status);
}
