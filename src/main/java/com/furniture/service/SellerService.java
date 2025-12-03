package com.furniture.service;

import java.util.List;

import com.furniture.domain.AccountStatus;
import com.furniture.exceptions.SellerException;
import com.furniture.modal.Seller;

public interface SellerService {

    Seller getSellerProfile(String jwt) throws Exception;
    Seller createSeller(Seller seller) throws Exception;
    Seller getSellerById(Long id) throws SellerException;
    Seller getSellerByEmail(String email) throws Exception;

    List<Seller> getAllSellers(AccountStatus status);

    Seller updateSeller(Long id, Seller seller) throws Exception;
    void deleteSeller(Long id) throws Exception;
    Seller verifyEmail(String email, String otp) throws Exception;
    Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws Exception;

}
