package com.furniture.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.furniture.config.JwtProvider;
import com.furniture.domain.AccountStatus;
import com.furniture.domain.USER_ROLE;
import com.furniture.exceptions.SellerException;
import com.furniture.modal.Address;
import com.furniture.modal.Seller;
import com.furniture.repository.AddressRepository;
import com.furniture.repository.SellerRepository;
import com.furniture.service.SellerService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private  final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;

    @Override
    public Seller getSellerProfile(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromToken(jwt);

        return this.getSellerByEmail(email);
    }

    @Override
    public Seller createSeller(@NonNull Seller seller) throws Exception {
        Seller sellerExist = sellerRepository.findByEmail(seller.getEmail());
        if (sellerExist != null) {
            throw new Exception("seller already exist, used different email");
        }
        Address pickupAddress = seller.getPickupAddress();
        if (pickupAddress == null) {
            throw new Exception("Pickup address cannot be null");
        }
        Address savedAddress = addressRepository.save(pickupAddress);

        Seller newSeller = new Seller();
        newSeller.setEmail(seller.getEmail());
        newSeller.setPassword(passwordEncoder.encode(seller.getPassword()));
        newSeller.setSellerName(seller.getSellerName());
        newSeller.setPickupAddress(savedAddress);
        newSeller.setMST(seller.getMST());
        newSeller.setRole(USER_ROLE.ROLE_SELLER);
        newSeller.setMobile(seller.getMobile());
        newSeller.setBankDetails(seller.getBankDetails());
        newSeller.setBussinessDetails(seller.getBussinessDetails());

        return sellerRepository.save(newSeller);
    }

    @Override
    public Seller getSellerById(@NonNull Long id) throws SellerException {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new SellerException("seller not found with id " + id));
    }

    @Override
    public Seller getSellerByEmail(String email) throws Exception {
        Seller seller = sellerRepository.findByEmail(email);
        if (seller == null) {
            throw new Exception("seller not found ...");
        }
        return seller;
    }

    @Override
    public List<Seller> getAllSellers(AccountStatus status) {
        if (status == null) {
            return sellerRepository.findAll();
        }
        return sellerRepository.findByAccountStatus(status);
    }

    @Override
    public Seller updateSeller(Long id, Seller seller) throws Exception {
        Seller existingSeller = this.getSellerById(id);

        if(seller.getSellerName() != null) {
            existingSeller.setSellerName(seller.getSellerName());
        }
        if(seller.getMobile() != null) {
            existingSeller.setMobile(seller.getMobile());
        }
        if(seller.getEmail() != null) {
            existingSeller.setEmail(seller.getEmail());
        }
        if(seller.getBussinessDetails() != null
                && seller.getBussinessDetails().getBussinessName() != null) {
            existingSeller.getBussinessDetails().setBussinessName(
                    seller.getBussinessDetails().getBussinessName()
            );
        }

        if(seller.getBankDetails() != null
                && seller.getBankDetails().getAccountHolderName() != null
                && seller.getBankDetails().getSwiftCode() != null
                && seller.getBankDetails().getAccountNumber() != null) {

            existingSeller.getBankDetails().setAccountHolderName(
                    seller.getBankDetails().getAccountHolderName()
            );
            existingSeller.getBankDetails().setAccountNumber(
                    seller.getBankDetails().getAccountNumber()
            );
            existingSeller.getBankDetails().setSwiftCode(
                    seller.getBankDetails().getSwiftCode()
            );
        }

        if(seller.getPickupAddress() != null
                && seller.getPickupAddress().getAddress() != null
                && seller.getPickupAddress().getCity() != null
                && seller.getPickupAddress().getMobile() != null
                && seller.getPickupAddress().getWard() != null) {
            existingSeller.getPickupAddress().setAddress(
                    seller.getPickupAddress().getAddress()
            );
            existingSeller.getPickupAddress().setCity(seller.getPickupAddress().getCity());
            existingSeller.getPickupAddress().setMobile(seller.getPickupAddress().getMobile());
            existingSeller.getPickupAddress().setWard(seller.getPickupAddress().getWard());
            existingSeller.getPickupAddress().setPinCode(seller.getPickupAddress().getPinCode());
        }

        if(seller.getMST() != null) {
            existingSeller.setMST(seller.getMST());
        }

        if (existingSeller != null) {
            return sellerRepository.save(existingSeller);
        }
        throw new Exception("Seller not found");
    }

    @Override
    public void deleteSeller(@NonNull Long id) throws Exception {

        Seller seller = getSellerById(id);
        if (seller != null) {
            sellerRepository.delete(seller);
        }

    }

    @Override
    public Seller verifyEmail(String email, String otp) throws Exception {
        Seller seller = getSellerByEmail(email);

        // Cập nhật trạng thái
        seller.setEmailVerified(true);
        seller.setAccountStatus(AccountStatus.ACTIVE); // <--- THÊM DÒNG NÀY

        return sellerRepository.save(seller);
    }

    @Override
    public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws Exception {


        Seller seller = getSellerById(sellerId);
        seller.setAccountStatus(status);

        return sellerRepository.save(seller);
    }
}
