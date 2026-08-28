package com.example.rent_it.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Data
public class RoommateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private String status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name="post_id")
    private RoommatePost post;

    @ManyToOne
    @JoinColumn(name="sender_id")
    private User sender;
}
