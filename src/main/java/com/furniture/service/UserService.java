package com.furniture.service;

import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Address;
import com.furniture.modal.User;

import java.util.List;

public interface UserService {

     User findUserByJwtToken(String jwt) throws Exception;
     User findUserByEmail(String email) throws Exception;
     List<User> findAllUsers();
     User updateRole(Long userId, USER_ROLE role) throws Exception;
     User createAddress(User user, Address address) throws Exception;
}
