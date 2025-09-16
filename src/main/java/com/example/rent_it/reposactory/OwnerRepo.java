package com.example.rent_it.reposactory;

import com.example.rent_it.Entitys.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepo extends JpaRepository<Owner,Integer> {
}
