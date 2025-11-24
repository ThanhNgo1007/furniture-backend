package com.furniture.service.impl;

import com.furniture.config.JwtProvider;
import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Address;
import com.furniture.modal.User;
import com.furniture.repository.AddressRepository;
import com.furniture.repository.UserRepository;
import com.furniture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public User updateRole(Long userId, USER_ROLE role) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public User createAddress(User user, Address address) {
        // 1. Lưu địa chỉ vào bảng Address trước
        Address savedAddress = addressRepository.save(address);

        // 2. Thêm địa chỉ vào danh sách của User
        user.getAddresses().add(savedAddress);

        // 3. Lưu User để cập nhật mối quan hệ
        return userRepository.save(user);
    }
}
