package com.example.rent_it.Repository;

import com.example.rent_it.Entity.RoommatePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoommateRepository extends JpaRepository<RoommatePost,Long> {
    List<RoommatePost> findByCityContainingIgnoreCase(String city);
}
