package com.example.rent_it.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RoomInterestRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private String status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name="room_post_id")
    private RoomPost roomPost;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}
