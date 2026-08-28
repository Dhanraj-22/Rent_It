package com.example.rent_it.Repository;

import com.example.rent_it.Entity.RoomPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomPostRepository extends JpaRepository<RoomPost,Long> {
    List<RoomPost> findByCityContainingIgnoreCase(String city);
}
