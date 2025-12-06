package com.furniture.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.config.JwtProvider;
import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Address;
import com.furniture.modal.User;
import com.furniture.repository.AddressRepository;
import com.furniture.repository.UserRepository;
import com.furniture.service.UserService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AddressRepository addressRepository;

    @Override
    public User findUserByJwtToken(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromToken(jwt);

        return this.findUserByEmail(email);
    }

    @Override
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("user not found with email - " + email);
        }
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateRole(@NonNull Long userId, USER_ROLE role) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public User createAddress(User user, @NonNull Address address) {
        // If user has no addresses, set this one as default
        if (user.getAddresses().isEmpty()) {
            address.setDefault(true);
        }
        
        // 1. Lưu địa chỉ vào bảng Address trước
        Address savedAddress = addressRepository.save(address);

        // 2. Thêm địa chỉ vào danh sách của User
        user.getAddresses().add(savedAddress);

        // 3. Lưu User để cập nhật mối quan hệ
        return userRepository.save(user);
    }

    @Override
    public User updateAddress(@NonNull Long userId, @NonNull Long addressId, Address addressRequest) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new Exception("Address not found"));

        if (!user.getAddresses().contains(address)) {
            throw new Exception("Address does not belong to user");
        }

        address.setName(addressRequest.getName());
        address.setMobile(addressRequest.getMobile());
        address.setAddress(addressRequest.getAddress());
        address.setCity(addressRequest.getCity());
        address.setWard(addressRequest.getWard());
        address.setLocality(addressRequest.getLocality());
        address.setPinCode(addressRequest.getPinCode());

        addressRepository.save(address);
        return user;
    }

    @Override
    public User deleteAddress(@NonNull Long userId, @NonNull Long addressId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // Check if address exists in user's list
        boolean hasAddress = user.getAddresses().stream()
                .anyMatch(a -> a.getId().equals(addressId));
        
        if (!hasAddress) {
            throw new Exception("Address not found or does not belong to user");
        }

        // Remove from user's list first
        user.getAddresses().removeIf(a -> a.getId().equals(addressId));
        userRepository.save(user);

        // Then delete the address by ID
        addressRepository.deleteById(addressId);

        return user;
    }

    @Override
    public User setDefaultAddress(@NonNull Long userId, @NonNull Long addressId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        boolean addressFound = false;
        for (Address address : user.getAddresses()) {
            if (address.getId().equals(addressId)) {
                address.setDefault(true);
                addressFound = true;
            } else {
                address.setDefault(false);
            }
            addressRepository.save(address);
        }

        if (!addressFound) {
            throw new Exception("Address not found in user's list");
        }

        return user;
    }

    @Override
    public User updateUserProfile(String jwt, String fullName, String mobile) throws Exception {
        User user = findUserByJwtToken(jwt);

        // Validation - fullName
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new Exception("Full name cannot be empty");
        }
        if (fullName.trim().length() < 2) {
            throw new Exception("Full name must be at least 2 characters");
        }

        // Validation - mobile
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new Exception("Mobile number cannot be empty");
        }
        // Vietnamese phone: 10-11 digits starting with 0
        if (!mobile.matches("^0\\d{9,10}$")) {
            throw new Exception("Invalid mobile number format. Must be 10-11 digits starting with 0");
        }

        user.setFullName(fullName.trim());
        user.setMobile(mobile.trim());

        return userRepository.save(user);
    }
}
