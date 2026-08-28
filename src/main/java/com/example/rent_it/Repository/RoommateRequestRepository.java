package com.example.rent_it.Repository;

import com.example.rent_it.Entity.RoommateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoommateRequestRepository extends JpaRepository<RoommateRequest, Long> {
    List<RoommateRequest> findByPostId(Long postId);
}
