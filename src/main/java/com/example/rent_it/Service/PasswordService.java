package com.example.rent_it.Service;

import com.example.rent_it.DTO.ResetPasswordRequest;

public interface PasswordService {
    String sendOtp(String email);
    String resetPassword(ResetPasswordRequest request);
}
