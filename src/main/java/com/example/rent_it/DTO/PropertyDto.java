package com.example.rent_it.DTO;

import lombok.Data;

import java.util.List;

@Data
public class PropertyDto {

    private Long id;

    private String title;

    private String description;

    private String address;

    private Double rent;

    private Double deposit;

    private String city;

    private String bhkType;

    private Boolean available;

    private Long ownerId;

    private String ownerName;

    private List<String> images;
}
