package com.example.rent_it.Repository;

import com.example.rent_it.Entity.RoomInterestRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomInterestRepository extends JpaRepository<RoomInterestRequest,Long> {
    List<RoomInterestRequest> findByRoomPostId(Long postId);
}
