package com.example.rent_it.Controller;

import com.example.rent_it.Dto.OwnerDto;
import com.example.rent_it.Entitys.Owner;
import com.example.rent_it.ServiceImpl.OwnerServiceImpl;
import com.example.rent_it.reposactory.OwnerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner")
public class OwnerController {
    @Autowired
    private OwnerServiceImpl ownerService;
    @PostMapping("/add")
    public ResponseEntity<OwnerDto> createOwner(@RequestBody OwnerDto ownerDto){
        OwnerDto owner=ownerService.createOwner(ownerDto);
        return new ResponseEntity<>(owner, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{ownerId}")
    public String deleteOwner( @PathVariable @RequestBody int ownerId){
        ownerService.deleteOwner(ownerId);
        return "Deleted"+ownerId;
    }
}
