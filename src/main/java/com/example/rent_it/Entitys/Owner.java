package com.example.rent_it.Entitys;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Setter
@Getter
@Entity
public class Owner {
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
 private int ownerId;
 private String ownerName;
 private String ownerEmail;
 private String ownerPhone;
 @OneToMany(mappedBy = "owner")
 private List<Flates> flates;

}
