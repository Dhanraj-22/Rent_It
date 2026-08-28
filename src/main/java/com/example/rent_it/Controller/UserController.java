package com.example.rent_it.Controller;

import com.example.rent_it.DTO.UserDto;
import com.example.rent_it.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("register")
    public ResponseEntity<UserDto> createNewUser(@RequestBody UserDto userDto){
        UserDto userdto=this.userService.createNewUser(userDto);
        return ResponseEntity.ok(userdto);
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<UserDto> varifyOtp(@RequestParam String email, @RequestParam String otp) {
        return ResponseEntity.ok(userService.varifyOtp(email, otp));
    }

    @GetMapping("id/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id){
        UserDto userDto=this.userService.findById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("email/{email}")
    public ResponseEntity<UserDto> findByEmail(@PathVariable String email){
        UserDto userDto=this.userService.findByEmail(email);
        return ResponseEntity.ok(userDto);
    }
    @GetMapping("allUsers")
    public ResponseEntity<List<UserDto>> findAll() {
        List<UserDto> userDtos = this.userService.findAll();
        return ResponseEntity.ok(userDtos);
    }

}
