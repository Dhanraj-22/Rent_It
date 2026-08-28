package com.example.rent_it.Controller;

import com.example.rent_it.DTO.RoomInterestRequestDto;
import com.example.rent_it.DTO.RoomPostDto;
import com.example.rent_it.Service.RoomPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomPostController {
    private final RoomPostService roomPostService;
    @PostMapping(value = "/{ownerId}",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomPostDto> createRoomPost(@RequestPart("room") RoomPostDto roomPostDto
    ,@RequestPart(value = "images", required = false) MultipartFile [] images
    ,@PathVariable Long ownerId) throws IOException {
        return ResponseEntity.ok(roomPostService.createRoomPost(roomPostDto, images, ownerId));
    }

    @GetMapping
    public ResponseEntity<List<RoomPostDto>> getAllRoomPosts() {
        return ResponseEntity.ok(roomPostService.getAllRoomPosts());
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomPostDto> getRoomPost(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomPostService.getRoomPost(roomId));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<RoomPostDto>> getRoomByCity(@PathVariable String city) {
        return ResponseEntity.ok(roomPostService.getRoomsByCity(city));
    }

    @PutMapping(value = "/{roomId}/{ownerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomPostDto> updateRoomPost(
            @PathVariable Long roomId,
            @PathVariable Long ownerId,
            @RequestPart("room") RoomPostDto roomPostDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images) throws IOException {
        return ResponseEntity.ok(roomPostService.updateRoomPost(roomId, roomPostDto, images, ownerId));
    }

    @DeleteMapping("/{roomId}/{ownerId}")
    public ResponseEntity<String> deleteRoomPost(@PathVariable Long roomId,@PathVariable Long ownerId) {
        roomPostService.deleteRoomsByRoomPostId(roomId,ownerId);
        return ResponseEntity.ok("Room post deleted");
    }

    @PostMapping("/{roomId}/interest/{userId}")
    public ResponseEntity<RoomInterestRequestDto> sendInterest(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            @RequestBody RoomInterestRequestDto requestDto) {
        String message = requestDto == null ? "" : requestDto.getMessage();
        return ResponseEntity.ok(roomPostService.sendRoomInterestRequest(roomId,userId,message));
    }

    @GetMapping("/{roomId}/requests")
    public ResponseEntity<List<RoomInterestRequestDto>> getRoomInterestRequests(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomPostService.getRoomInterestRequests(roomId));
    }

    @PutMapping("/request/{id}/accept")
    public ResponseEntity<String> acceptRequest(
            @PathVariable Long id){

        roomPostService.acceptRequest(id);

        return ResponseEntity.ok("Request Accepted");
    }

    @PutMapping("/request/{id}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id){
        roomPostService.rejectRequest(id);
        return ResponseEntity.ok("Request Rejected");
    }
}
