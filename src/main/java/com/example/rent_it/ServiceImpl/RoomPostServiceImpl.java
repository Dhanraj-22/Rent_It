package com.example.rent_it.ServiceImpl;

import com.example.rent_it.DTO.RoomInterestRequestDto;
import com.example.rent_it.DTO.RoomPostDto;
import com.example.rent_it.Entity.Role;
import com.example.rent_it.Entity.RoomImage;
import com.example.rent_it.Entity.RoomInterestRequest;
import com.example.rent_it.Entity.RoomPost;
import com.example.rent_it.Entity.User;
import com.example.rent_it.Exception.ResourceNotFoundException;
import com.example.rent_it.Repository.RoomInterestRepository;
import com.example.rent_it.Repository.RoomPostRepository;
import com.example.rent_it.Repository.UserRepositoty;
import com.example.rent_it.Service.EmailService;
import com.example.rent_it.Service.FileService;
import com.example.rent_it.Service.RoomPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomPostServiceImpl implements RoomPostService {
    private final RoomPostRepository roomPostRepository;
    private final RoomInterestRepository roomInterestRepository;
    private final UserRepositoty userRepositoty;
    private final FileService fileService;
    private final EmailService emailService;

    @Value("${property.image.path:uploads}")
    private String path;

    @Value("${app.base-url:http://localhost:8086}")
    private String baseUrl;

    @Override
    public RoomPostDto createRoomPost(RoomPostDto roomPostDto, MultipartFile[] files, Long ownerId) throws IOException {
        User owner = findOwner(ownerId);
        RoomPost roomPost = new RoomPost();
        copyRoomFields(roomPost, roomPostDto);
        roomPost.setOwner(owner);
        roomPost.setAvailable(roomPostDto.getAvailable() == null || roomPostDto.getAvailable());
        roomPost.setCreatedAt(LocalDateTime.now());
        roomPost.setImages(new ArrayList<>());

        addImages(roomPost, files);

        return toDto(roomPostRepository.save(roomPost));
    }

    @Override
    public RoomPostDto getRoomPost(Long roomPostId) {
        return toDto(findRoom(roomPostId));
    }

    @Override
    public List<RoomPostDto> getAllRoomPosts() {
        return roomPostRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<RoomPostDto> getRoomsByCity(String city) {
        return roomPostRepository.findByCityContainingIgnoreCase(city)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public RoomPostDto updateRoomPost(
            Long roomPostId,
            RoomPostDto roomPostDto,
            MultipartFile[] files,
            Long ownerId) throws IOException {

        RoomPost roomPost = findRoom(roomPostId);
        verifyOwner(roomPost, ownerId);
        copyRoomFields(roomPost, roomPostDto);

        if (hasFiles(files)) {
            if (roomPost.getImages() == null) {
                roomPost.setImages(new ArrayList<>());
            } else {
                roomPost.getImages().clear();
            }
            addImages(roomPost, files);
        }

        return toDto(roomPostRepository.save(roomPost));
    }

    @Override
    public void deleteRoomsByRoomPostId(Long roomPostId, Long ownerId) {
        RoomPost roomPost = findRoom(roomPostId);
        verifyOwner(roomPost, ownerId);
        roomPostRepository.delete(roomPost);
    }

    @Override
    public RoomInterestRequestDto sendRoomInterestRequest(Long roomPostId, Long userId, String message) {
        RoomPost roomPost = findRoom(roomPostId);
        User user = userRepositoty.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = " + userId));

        RoomInterestRequest request = new RoomInterestRequest();
        request.setRoomPost(roomPost);
        request.setUser(user);
        request.setMessage(message == null ? "" : message);
        request.setStatus("Pending");
        request.setCreatedAt(LocalDateTime.now());

        RoomInterestRequest savedRequest = roomInterestRepository.save(request);

        if (roomPost.getOwner() != null && roomPost.getOwner().getEmail() != null) {
            String emailMessage = "New interest has been sent to\n\n"
                    + "User: " + user.getName() + "\n"
                    + "Message: " + savedRequest.getMessage() + "\n\n"
                    + "Accept: " + baseUrl.replaceAll("/$", "") + "/api/rooms/request/" + savedRequest.getId() + "/accept\n"
                    + "Reject: " + baseUrl.replaceAll("/$", "") + "/api/rooms/request/" + savedRequest.getId() + "/reject";

            emailService.sendEmail(roomPost.getOwner().getEmail(), "New Room Request", emailMessage);
        }

        return toRequestDto(savedRequest);
    }

    @Override
    public List<RoomInterestRequestDto> getRoomInterestRequests(Long roomPostId) {
        findRoom(roomPostId);
        return roomInterestRepository.findByRoomPostId(roomPostId)
                .stream()
                .map(this::toRequestDto)
                .toList();
    }

    @Override
    public void acceptRequest(Long requestId) {
        RoomInterestRequest request = findRequest(requestId);
        request.setStatus("Accepted");
        roomInterestRepository.save(request);
        emailService.sendEmail(
                request.getUser().getEmail(),
                "Room Request Accepted",
                "Your request for the room has been accepted"
        );
    }

    @Override
    public void rejectRequest(Long requestId) {
        RoomInterestRequest request = findRequest(requestId);
        request.setStatus("Rejected");
        roomInterestRepository.save(request);
        emailService.sendEmail(
                request.getUser().getEmail(),
                "Room Request Rejected",
                "Your request for the room has been rejected"
        );
    }

    private User findOwner(Long ownerId) {
        User owner = userRepositoty.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id = " + ownerId));

        if (owner.getRole() != Role.OWNER && owner.getRole() != Role.ADMIN) {
            throw new ResourceNotFoundException("Only OWNER users can create room posts");
        }

        return owner;
    }

    private RoomPost findRoom(Long roomPostId) {
        return roomPostRepository.findById(roomPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Room post not found with id = " + roomPostId));
    }

    private RoomInterestRequest findRequest(Long requestId) {
        return roomInterestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id = " + requestId));
    }

    private void verifyOwner(RoomPost roomPost, Long ownerId) {
        if (roomPost.getOwner() == null || !roomPost.getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException("Unauthorized owner");
        }
    }

    private void copyRoomFields(RoomPost roomPost, RoomPostDto roomPostDto) {
        if (roomPostDto.getTitle() != null) {
            roomPost.setTitle(roomPostDto.getTitle());
        }
        if (roomPostDto.getDescription() != null) {
            roomPost.setDescription(roomPostDto.getDescription());
        }
        if (roomPostDto.getCity() != null) {
            roomPost.setCity(roomPostDto.getCity());
        }
        if (roomPostDto.getAddress() != null) {
            roomPost.setAddress(roomPostDto.getAddress());
        }
        if (roomPostDto.getRent() != null) {
            roomPost.setRent(roomPostDto.getRent());
        }
        if (roomPostDto.getCapacity() != null) {
            roomPost.setCapacity(roomPostDto.getCapacity());
        }
        if (roomPostDto.getAvailable() != null) {
            roomPost.setAvailable(roomPostDto.getAvailable());
        }
    }

    private void addImages(RoomPost roomPost, MultipartFile[] files) throws IOException {
        if (files == null) {
            return;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String fileName = fileService.uploadImage(path, file);
            RoomImage roomImage = new RoomImage();
            roomImage.setImageUrl(fileName);
            roomImage.setRoomPost(roomPost);
            roomPost.getImages().add(roomImage);
        }
    }

    private boolean hasFiles(MultipartFile[] files) {
        if (files == null) {
            return false;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private RoomPostDto toDto(RoomPost roomPost) {
        RoomPostDto dto = new RoomPostDto();
        dto.setId(roomPost.getId());
        dto.setTitle(roomPost.getTitle());
        dto.setDescription(roomPost.getDescription());
        dto.setCity(roomPost.getCity());
        dto.setAddress(roomPost.getAddress());
        dto.setRent(roomPost.getRent());
        dto.setCapacity(roomPost.getCapacity());
        dto.setAvailable(roomPost.getAvailable());
        dto.setCreatedAt(roomPost.getCreatedAt());

        if (roomPost.getOwner() != null) {
            dto.setOwnerId(roomPost.getOwner().getId());
            dto.setOwnerName(roomPost.getOwner().getName());
        }

        dto.setImages(roomPost.getImages() == null
                ? List.of()
                : roomPost.getImages()
                        .stream()
                        .map(RoomImage::getImageUrl)
                        .map(this::imageUrl)
                        .toList());

        return dto;
    }

    private RoomInterestRequestDto toRequestDto(RoomInterestRequest request) {
        RoomInterestRequestDto dto = new RoomInterestRequestDto();
        dto.setId(request.getId());
        dto.setMessage(request.getMessage());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());

        if (request.getRoomPost() != null) {
            dto.setRoomId(request.getRoomPost().getId());
        }

        if (request.getUser() != null) {
            dto.setUserId(request.getUser().getId());
            dto.setUserName(request.getUser().getName());
        }

        return dto;
    }

    private String imageUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return fileName;
        }
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            return fileName;
        }

        return baseUrl.replaceAll("/$", "") + "/uploads/" + fileName;
    }
}
