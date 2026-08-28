package com.example.rent_it.Repository;

import com.example.rent_it.Entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByCityContainingIgnoreCase(String city);
}
