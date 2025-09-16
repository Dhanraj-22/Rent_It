package com.example.rent_it.Dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OwnerDto {
    private int ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;

}
