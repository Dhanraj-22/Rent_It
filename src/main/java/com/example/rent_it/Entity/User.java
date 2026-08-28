package com.example.rent_it.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private String phone;

    private Boolean active = true;

    private Boolean emailVerified = false;

    @OneToMany(mappedBy = "owner",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Property> properties;

    @OneToMany(mappedBy = "owner",
            cascade = CascadeType.ALL)
    private List<RoomPost> roomPosts;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL)
    private List<RoommatePost> roommatePosts;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL)
    private List<RoomInterestRequest> roomRequests;

}
