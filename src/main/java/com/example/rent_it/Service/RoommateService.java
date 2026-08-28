package com.example.rent_it.Service;

import com.example.rent_it.DTO.RoommateDto;
import com.example.rent_it.DTO.RoommateRequestDto;
import com.example.rent_it.Entity.RoommateRequest;

import java.util.List;

public interface RoommateService {
       RoommateDto createPost(RoommateDto roommateDto,Long userId);
       List<RoommateDto> getAllPosts();
       List<RoommateDto> getPostsByCity(String city);
       void sendRequest(Long postId, Long userId, RoommateRequestDto   roommateRequestDto);
       List<RoommateRequestDto> getRequestsForPost(Long postId);
       void acceptRequest(Long requestId);
       void rejectRequest(Long requestId);
       void deletePost(Long postId);
}
