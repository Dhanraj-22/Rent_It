package com.example.rent_it.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoommateRequestDto {

    private Long id;
    @NotBlank(message = "A message is required")
    @Size(max = 500, message = "Message must be 500 characters or fewer")
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private Long postId;
    private Long senderId;
    private String senderName;
}
