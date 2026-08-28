package com.example.rent_it.Controller;

import com.example.rent_it.DTO.RoommateDto;
import com.example.rent_it.DTO.RoommateRequestDto;
import com.example.rent_it.Service.RoommateService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roommate")
@Data
public class RoommateController {
    private final RoommateService roommateService;


    @PostMapping("/createPost/{userId}")
    public ResponseEntity<RoommateDto> createRoommate(@RequestBody RoommateDto roommateDto,@PathVariable Long userId) {
        return ResponseEntity.ok(roommateService.createPost(roommateDto,userId));
      }


    @GetMapping("/getAllPost")
  public ResponseEntity<List<RoommateDto>> getAllPost(){
        return ResponseEntity.ok(roommateService.getAllPosts());
      }


      @GetMapping("/getPostByCity/{city}")
      public ResponseEntity<List<RoommateDto>> getPostByCity(@PathVariable String city){
        return ResponseEntity.ok(roommateService.getPostsByCity(city));
      }


      @PostMapping("/request/{postId}/{userId}")
      public ResponseEntity<String> sendRequest(@PathVariable Long postId,
                                                @PathVariable Long userId
              ,@RequestBody RoommateRequestDto roommateRequestDto){
         roommateService.sendRequest(postId,userId,roommateRequestDto);
         return ResponseEntity.ok("Request sent successfully");
      }

      @GetMapping("/request/post/{postId}")
      public ResponseEntity<List<RoommateRequestDto>> getRequestsForPost(@PathVariable Long postId){
        return ResponseEntity.ok(roommateService.getRequestsForPost(postId));
      }


      @PutMapping("/request/{requestId}/accept")
      public ResponseEntity<String> acceptRequest(@PathVariable Long requestId){
        roommateService.acceptRequest(requestId);
        return ResponseEntity.ok("Request accepted successfully");
      }

    @PutMapping("/request/{requestId}/reject")
      public ResponseEntity<String> rejectRequest(@PathVariable Long requestId){
        roommateService.rejectRequest(requestId);
        return ResponseEntity.ok("Request rejected successfully");
      }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {

        roommateService.deletePost(postId);

        return ResponseEntity.ok("Post deleted successfully");
    }

}
