package com.example.rent_it.Entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Entity
public class Flates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int flateId;
    private String Address;
    private boolean available;
    private String flateOwner;
    private String rent;
    @ManyToOne
    @JoinColumn(name = "owner_id")   // foreign key in flates table
    private Owner owner;

    @ManyToMany
    @JoinTable(
            name = "flat_bachelor",
            joinColumns = @JoinColumn(name = "flat_id"),
            inverseJoinColumns = @JoinColumn(name = "bachelor_id")
    )
    private List<Bechlors> bechlors;

    @OneToMany(mappedBy = "flate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> mediaFiles;
}
