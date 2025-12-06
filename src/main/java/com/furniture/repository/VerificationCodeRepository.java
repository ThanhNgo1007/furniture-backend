package com.furniture.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.modal.VerificationCode;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    VerificationCode findByEmail(String email);

    VerificationCode findByOtp(String otp);

}
