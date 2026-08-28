package com.example.rent_it.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
    private String phone;
    private Boolean active;
    private Boolean emailVerified;

}
