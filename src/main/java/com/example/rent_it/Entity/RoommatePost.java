package com.example.rent_it.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RoommatePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String city;
     private String rent;
    private String genderPreference;
    private Boolean active;
    private String foodPreference;
    private boolean smoking;
    private String occupation;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private  User user;
}
