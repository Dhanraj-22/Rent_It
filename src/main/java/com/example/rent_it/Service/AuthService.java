package com.example.rent_it.Service;

import com.example.rent_it.Auth.LoginRequest;
import com.example.rent_it.Auth.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
}
