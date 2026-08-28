package com.example.rent_it.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoomPostDto {

    private Long id;

    private String title;

    private String description;

    private String city;

    private String address;

    private Double rent;

    private Integer capacity;

    private Boolean available;

    private LocalDateTime createdAt;

    private Long ownerId;

    private String ownerName;

    private List<String> images;
}
