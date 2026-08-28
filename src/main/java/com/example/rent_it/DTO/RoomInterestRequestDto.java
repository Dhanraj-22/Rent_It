package com.example.rent_it.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomInterestRequestDto {
    private Long id;

    private String message;

    private String status;

    private LocalDateTime createdAt;

    private Long roomId;

    private Long userId;

    private String userName;
}
