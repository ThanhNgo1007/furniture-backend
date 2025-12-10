package com.furniture.service;

import java.util.List;

import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Address;
import com.furniture.modal.User;

import lombok.NonNull;
import org.springframework.data.domain.Page;

public interface UserService {

     User findUserByJwtToken(String jwt) throws Exception;
     User findUserByEmail(String email) throws Exception;
     List<User> findAllUsers();
     Page<User> findAllUsersPaginated(org.springframework.data.domain.Pageable pageable);
     User updateRole(@NonNull Long userId, USER_ROLE role) throws Exception;
     User createAddress(User user, @NonNull Address address) throws Exception;
     User updateAddress(@NonNull Long userId, @NonNull Long addressId, Address address) throws Exception;
     User deleteAddress(@NonNull Long userId, @NonNull Long addressId) throws Exception;
     User setDefaultAddress(@NonNull Long userId, @NonNull Long addressId) throws Exception;
     User updateUserProfile(String jwt, String fullName, String mobile) throws Exception;
}
