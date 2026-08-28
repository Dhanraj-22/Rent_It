package com.example.rent_it.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoommateRequestDto {

    private Long id;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private Long postId;
    private Long senderId;
    private String senderName;
}
