package com.furniture.service.impl;

import com.furniture.domain.USER_ROLE;
import com.furniture.modal.User;
import com.furniture.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataIni implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args){
        initializeAdminUser();
    }

    private void initializeAdminUser(){
        String adminUsername = "thanhb2204966@student.ctu.edu.vn";

        if(userRepository.findByEmail(adminUsername) == null){
            User adminUser = new User();

            adminUser.setPassword(passwordEncoder.encode("Admin@123"));
            adminUser.setFullName("B2204966");
            adminUser.setEmail(adminUsername);
            adminUser.setRole(USER_ROLE.ROLE_ADMIN);

            userRepository.save(adminUser);
        }
    }
}
