package com.example.rent_it.Service;

import com.example.rent_it.DTO.RoomInterestRequestDto;
import com.example.rent_it.DTO.RoomPostDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RoomPostService {
    RoomPostDto createRoomPost(RoomPostDto roomPostDto, MultipartFile [] files,Long ownerId) throws IOException;
    RoomPostDto getRoomPost(Long roomPostId);
    List<RoomPostDto> getAllRoomPosts();
    List<RoomPostDto> getRoomsByCity(String city);
    RoomPostDto updateRoomPost(Long roomPostId, RoomPostDto roomPostDto, MultipartFile[] files, Long ownerId) throws IOException;
    void deleteRoomsByRoomPostId(Long roomPostId,Long ownerId);
    RoomInterestRequestDto sendRoomInterestRequest(Long roomPostId,Long userId,String message);
    List<RoomInterestRequestDto> getRoomInterestRequests(Long roomPostId);
    void acceptRequest(Long requestId);
    void rejectRequest(Long requestId);
}
