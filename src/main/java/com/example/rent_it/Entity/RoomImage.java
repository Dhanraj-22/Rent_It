package com.example.rent_it.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class RoomImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name="room_post_id")
    private RoomPost roomPost;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}
