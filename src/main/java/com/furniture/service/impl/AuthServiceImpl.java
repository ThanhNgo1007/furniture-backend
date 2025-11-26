package com.furniture.service.impl;

import com.furniture.config.JwtProvider;
import com.furniture.domain.AccountStatus;
import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Cart;
import com.furniture.modal.Seller;
import com.furniture.modal.User;
import com.furniture.modal.VerificationCode;
import com.furniture.repository.CartRepository;
import com.furniture.repository.SellerRepository;
import com.furniture.repository.UserRepository;
import com.furniture.repository.VerificationCodeRepository;
import com.furniture.request.LoginRequest;
import com.furniture.response.AuthResponse;
import com.furniture.request.SignupRequest;
import com.furniture.service.AuthService;
import com.furniture.service.EmailService;
import com.furniture.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final CustomUserServiceImpl customUserService;
    private final SellerRepository sellerRepository;

    // AuthServiceImpl.java

    @Override
    public void sentLoginOtp(String email, USER_ROLE role) throws Exception {
        String SIGNING_PREFIX = "signing_";

        if (email.startsWith(SIGNING_PREFIX)) {
            email = email.substring(SIGNING_PREFIX.length());

            // --- SỬA LỖI TẠI ĐÂY ---
            // Thêm check "role != null" trước khi so sánh
            if (role != null && role.equals(USER_ROLE.ROLE_SELLER)) {
                Seller seller = sellerRepository.findByEmail(email);
                if (seller == null) {
                    throw new Exception("seller not found");
                }
            } else {
                // Nếu role là null hoặc là ROLE_CUSTOMER thì chạy vào đây
                System.out.println("email" + email);
                User user = userRepository.findByEmail(email);
                if (user == null) {
                    throw new Exception("user not exist with provided email");
                }
            }
            // --- KẾT THÚC SỬA ---
        }

        // Tìm hoặc tạo VerificationCode
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);
        if (verificationCode == null) {
            verificationCode = new VerificationCode();
            verificationCode.setEmail(email);
        }

        // Tạo OTP mới
        String otp = OtpUtil.generateOtp();
        verificationCode.setOtp(otp);

        // ✅ QUAN TRỌNG: Set ExpiryDate
        verificationCode.setExpiryDate(LocalDateTime.now().plusMinutes(5));

        // Lưu vào database
        verificationCodeRepository.save(verificationCode);

        // Gửi email
        String subject = "AptDeco login/signup otp";
        String text = "Your login/signup otp is - " + otp;
        emailService.sendVerificationOtpEmail(email, otp, subject, text);
    }

    @Override
    public String createUser(SignupRequest req) throws Exception {

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(req.getEmail());

        if (verificationCode == null || !verificationCode.getOtp().equals(req.getOtp())) {
            throw new Exception("wrong otp...");
        }

        User user = userRepository.findByEmail(req.getEmail());

        if (user == null) {
            User createdUser = new User();
            createdUser.setEmail(req.getEmail());
            createdUser.setFullName(req.getFullName());
            createdUser.setRole(USER_ROLE.ROLE_CUSTOMER);
            createdUser.setMobile("0907941448");
            createdUser.setPassword(passwordEncoder.encode(req.getOtp()));

            user=userRepository.save(createdUser);

            Cart cart = new Cart();
            cart.setUser(user);
            cart.setTotalSellingPrice(BigDecimal.ZERO);
            cart.setTotalMsrpPrice(BigDecimal.ZERO);
            cartRepository.save(cart);
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(USER_ROLE.ROLE_CUSTOMER.toString()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(req.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);


        return jwtProvider.generateAccessToken(authentication);
    }

    @Override
    public AuthResponse signing(LoginRequest req) {
        String username = req.getEmail();
        String otp = req.getOtp();

        Authentication authentication = authenticate(username, otp);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sửa: Tạo cả 2 token
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(accessToken);
        authResponse.setRefreshToken(refreshToken); // Set Refresh Token
        authResponse.setMessage("Login success");

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();
        authResponse.setRole(USER_ROLE.valueOf(roleName));

        return authResponse;
    }

    private Authentication authenticate(String username, String otp) {
        // 1. Load UserDetails (Giữ nguyên username có prefix để phân biệt bảng)
        UserDetails userDetails = customUserService.loadUserByUsername(username);

        String SELLER_PREFIX = "seller_";
        String actualEmail = username; // Biến này sẽ lưu email gốc

        // 2. Xử lý logic Seller
        if (username.startsWith(SELLER_PREFIX)) {
            actualEmail = username.substring(SELLER_PREFIX.length()); // Cắt bỏ "seller_"

            // Logic kiểm tra trạng thái tài khoản (Giữ nguyên như bạn đã viết)
            Seller seller = sellerRepository.findByEmail(actualEmail);
            if (seller != null && seller.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
                try {
                    String newOtp = OtpUtil.generateOtp();
                    VerificationCode vc = verificationCodeRepository.findByEmail(actualEmail);
                    if (vc == null) {
                        vc = new VerificationCode();
                        vc.setEmail(actualEmail);
                    }
                    vc.setOtp(newOtp);
                    // Set thời gian hết hạn nếu có
                    // vc.setExpiryDate(LocalDateTime.now().plusMinutes(5));
                    verificationCodeRepository.save(vc);

                    String subject = "AptDeco Seller Account Verification (Resend)";
                    String frontend_url = "http://localhost:5173/verify-seller/" + newOtp;
                    String text = "Your account is not verified yet. Please verify using this link: " + frontend_url;
                    emailService.sendVerificationOtpEmail(actualEmail, newOtp, subject, text);
                } catch (Exception e) {
                    throw new BadCredentialsException("Failed to send verification email");
                }
                throw new BadCredentialsException("Account is not verified. A new verification email has been sent.");
            }
        }

        // 3. Tìm OTP bằng actualEmail (QUAN TRỌNG: Đã sửa từ username thành actualEmail)
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(actualEmail);

        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new BadCredentialsException("wrong otp");
        }

        // 4. Kiểm tra hết hạn
        if (verificationCode.getExpiryDate() != null
                && verificationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("OTP has expired");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}
