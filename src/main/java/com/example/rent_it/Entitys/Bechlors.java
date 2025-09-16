package com.example.rent_it.Entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter

public class Bechlors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private int BechlorId;
     private String BechlorName;
     private String email;
     private String phone;
     private String address;
     @ManyToMany(mappedBy = "bechlors")
     List<Flates> flates;


}
