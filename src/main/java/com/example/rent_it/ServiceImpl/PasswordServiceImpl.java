package com.example.rent_it.ServiceImpl;

import com.example.rent_it.DTO.ResetPasswordRequest;
import com.example.rent_it.Entity.PasswordResetOpt;
import com.example.rent_it.Entity.User;
import com.example.rent_it.Exception.ResourceNotFoundException;
import com.example.rent_it.Repository.PasswordResetOtpRepo;
import com.example.rent_it.Repository.UserRepositoty;
import com.example.rent_it.Service.EmailService;
import com.example.rent_it.Service.PasswordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {
    private final UserRepositoty userRepositoty;
    private final PasswordResetOtpRepo passwordResetOtpRepo;
    private final EmailService emailService;
    private  final PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public String sendOtp(String email) {
        User user=userRepositoty.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));
        passwordResetOtpRepo.deleteByEmail(email);
        String otp=String.valueOf(100000 + new Random().nextInt(900000));
        PasswordResetOpt entity=new PasswordResetOpt();
      entity.setEmail(email);
      entity.setOtp(otp);
      entity.setExpiryTime(LocalDateTime.now().plusMinutes(3));
      passwordResetOtpRepo.save(entity);
      emailService.sendEmail(email,"password reset:",otp);
        return "Otp sent successfully";
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetOpt savedOtp = passwordResetOtpRepo
                .findAllByEmailOrderByIdDesc(request.getEmail())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("OTP not found"));
          if(savedOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
              throw new RuntimeException("OTP is expired");
          }
          if(request.getOtp() == null || !savedOtp.getOtp().equals(request.getOtp())) {
              throw new RuntimeException("Invalid OTP");
          }
          User user= userRepositoty.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
          user.setPassword(passwordEncoder.encode(request.getNewPassword()));

          userRepositoty.save(user);
          passwordResetOtpRepo.delete(savedOtp);
           return "Password reset successfully";
    }
}
