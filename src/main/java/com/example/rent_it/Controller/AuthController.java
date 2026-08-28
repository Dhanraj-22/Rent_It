package com.example.rent_it.Controller;

import com.example.rent_it.Auth.LoginRequest;
import com.example.rent_it.Auth.LoginResponse;
import com.example.rent_it.DTO.ForgetPasswordRequest;
import com.example.rent_it.DTO.ResetPasswordRequest;
import com.example.rent_it.Service.AuthService;
import com.example.rent_it.Service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordService passwordService;
    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
    @PostMapping("/forget-password")
    public String ForgotPassword(@RequestBody ForgetPasswordRequest request)
    {
        return passwordService.sendOtp(request.getEmail());
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request)
    {
        return passwordService.resetPassword(request);
    }

}
