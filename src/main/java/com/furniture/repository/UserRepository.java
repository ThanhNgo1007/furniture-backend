package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.domain.USER_ROLE;
import com.furniture.modal.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    // Count users by role
    long countByRole(USER_ROLE role);
}
