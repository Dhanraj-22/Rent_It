package com.example.rent_it.ServiceImpl;

import com.example.rent_it.DTO.RoommateDto;
import com.example.rent_it.DTO.RoommateRequestDto;
import com.example.rent_it.Entity.RoommatePost;
import com.example.rent_it.Entity.RoommateRequest;
import com.example.rent_it.Entity.User;
import com.example.rent_it.Exception.ResourceNotFoundException;
import com.example.rent_it.Repository.RoommateRepository;
import com.example.rent_it.Repository.RoommateRequestRepository;
import com.example.rent_it.Repository.UserRepositoty;
import com.example.rent_it.Service.RoommateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoommateServiceImpl implements RoommateService {
    private final RoommateRepository roommateRepository;
    private final RoommateRequestRepository roommateRequestRepository;
    private final UserRepositoty userRepositoty;

    @Override
    public RoommateDto createPost(RoommateDto roommateDto, Long userId) {
        User user = userRepositoty.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = " + userId));

        RoommatePost post = new RoommatePost();
        post.setTitle(roommateDto.getTitle());
        post.setDescription(roommateDto.getDescription());
        post.setCity(roommateDto.getCity());
        post.setRent(roommateDto.getRent());
        post.setGenderPreference(roommateDto.getGenderPreference());
        post.setFoodPreference(roommateDto.getFoodPreference());
        post.setSmoking(roommateDto.isSmoking());
        post.setOccupation(roommateDto.getOccupation());
        post.setUser(user);
        post.setActive(true);
        post.setCreatedAt(LocalDateTime.now());

        return toDto(roommateRepository.save(post));
    }

    @Override
    public List<RoommateDto> getAllPosts() {
        return roommateRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<RoommateDto> getPostsByCity(String city) {
        return roommateRepository.findByCityContainingIgnoreCase(city)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void sendRequest(Long postId, Long userId, RoommateRequestDto roommateRequestDto) {
        RoommatePost post = roommateRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id = " + postId));

        User sender = userRepositoty.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = " + userId));

        RoommateRequest request = new RoommateRequest();
        request.setPost(post);
        request.setSender(sender);
        request.setMessage(roommateRequestDto == null ? "" : roommateRequestDto.getMessage());
        request.setStatus("Pending");
        request.setCreatedAt(LocalDateTime.now());
        roommateRequestRepository.save(request);
    }

    @Override
    public List<RoommateRequestDto> getRequestsForPost(Long postId) {
        roommateRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id = " + postId));

        return roommateRequestRepository.findByPostId(postId)
                .stream()
                .map(this::toRequestDto)
                .toList();
    }

    @Override
    public void acceptRequest(Long requestId) {
        RoommateRequest request = findRequest(requestId);
        request.setStatus("Accepted");
        roommateRequestRepository.save(request);
    }

    @Override
    public void rejectRequest(Long requestId) {
        RoommateRequest request = findRequest(requestId);
        request.setStatus("Rejected");
        roommateRequestRepository.save(request);
    }

    @Override
    public void deletePost(Long postId) {
        RoommatePost post = roommateRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id = " + postId));

        roommateRepository.delete(post);
    }

    private RoommateRequest findRequest(Long requestId) {
        return roommateRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id = " + requestId));
    }

    private RoommateDto toDto(RoommatePost post) {
        RoommateDto dto = new RoommateDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setCity(post.getCity());
        dto.setRent(post.getRent());
        dto.setGenderPreference(post.getGenderPreference());
        dto.setFoodPreference(post.getFoodPreference());
        dto.setSmoking(post.isSmoking());
        dto.setOccupation(post.getOccupation());
        dto.setActive(Boolean.TRUE.equals(post.getActive()));
        dto.setCreatedAt(post.getCreatedAt());

        if (post.getUser() != null) {
            dto.setUserId(post.getUser().getId());
            dto.setUserName(post.getUser().getName());
        }

        return dto;
    }

    private RoommateRequestDto toRequestDto(RoommateRequest request) {
        RoommateRequestDto dto = new RoommateRequestDto();
        dto.setId(request.getId());
        dto.setMessage(request.getMessage());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());

        if (request.getPost() != null) {
            dto.setPostId(request.getPost().getId());
        }

        if (request.getSender() != null) {
            dto.setSenderId(request.getSender().getId());
            dto.setSenderName(request.getSender().getName());
        }

        return dto;
    }
}
