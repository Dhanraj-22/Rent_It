package com.example.rent_it.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RoomPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String city;

    private String address;

    private Double rent;

    private Integer capacity;

    private Boolean available;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;

    @OneToMany(mappedBy = "roomPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<RoomImage> images;

}
