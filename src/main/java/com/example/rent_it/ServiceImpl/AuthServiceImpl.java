package com.example.rent_it.ServiceImpl;

import com.example.rent_it.Auth.JwtService;
import com.example.rent_it.Auth.LoginRequest;
import com.example.rent_it.Auth.LoginResponse;
import com.example.rent_it.Entity.User;
import com.example.rent_it.Exception.ResourceNotFoundException;
import com.example.rent_it.Repository.UserRepositoty;
import com.example.rent_it.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepositoty userRepositoty;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user=userRepositoty.findByEmail(loginRequest.getEmail())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        if(!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword()))
        {
            throw new RuntimeException("Wrong password");
        }
        if(!Boolean.TRUE.equals(user.getEmailVerified())){
            throw new RuntimeException("User not verified");
        }
        String token =jwtService.generateJwtToken(user.getEmail());
        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole() == null ? "USER" : user.getRole().name());
    }
}
