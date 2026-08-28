package com.example.rent_it.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoommateDto {
    private Long id;
    private String title;
    private String description;

    private String city;
    private String rent;



    private String genderPreference;
    private String foodPreference;
    private boolean smoking;
    private String occupation;
    private  boolean active;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
}
