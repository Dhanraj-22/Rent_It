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
@Table(name="properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String address;

    private Double rent;

    private Double deposit;

    private String city;

    private String bhkType;

    private Boolean available=true;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User owner;

    @OneToMany(mappedBy = "property",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PropertyImage> images;

}
