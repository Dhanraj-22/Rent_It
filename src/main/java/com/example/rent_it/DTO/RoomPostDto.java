package com.example.rent_it.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoomPostDto {

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

    @NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address must be 200 characters or fewer")
    private String address;

    @NotNull(message = "Rent is required")
    @Min(value = 1, message = "Rent must be greater than zero")
    private Double rent;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least one")
    private Integer capacity;

    private Boolean available;

    private LocalDateTime createdAt;

    private Long ownerId;

    private String ownerName;

    private List<String> images;
}
