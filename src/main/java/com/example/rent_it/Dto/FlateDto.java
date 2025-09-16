package com.example.rent_it.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlateDto {
    private int flateId;
    private String Address;
    private boolean available;
    private String flateOwner;
    private String rent;
    private List<MediaDto> mediafiles;

}
