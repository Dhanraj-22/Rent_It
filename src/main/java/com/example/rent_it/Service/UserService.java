package com.example.rent_it.Service;

import com.example.rent_it.DTO.UserDto;
import com.example.rent_it.Entity.User;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {
    UserDto createNewUser(UserDto userDto);
    UserDto findById(Long id);
    UserDto findByEmail(String email);

   List<UserDto> findAll();
   UserDto varifyOtp(String email, String otp);
}
