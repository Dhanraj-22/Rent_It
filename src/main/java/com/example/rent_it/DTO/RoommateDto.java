package com.example.rent_it.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoommateDto {
    private Long id;
    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must be 120 characters or fewer")
    private String title;
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    @NotBlank(message = "City is required")
    @Size(max = 80, message = "City must be 80 characters or fewer")
    private String city;
    @NotBlank(message = "Rent is required")
    @Size(max = 40, message = "Rent must be 40 characters or fewer")
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
