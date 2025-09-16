package com.example.rent_it.Controller;

import com.example.rent_it.Dto.FlateDto;
import com.example.rent_it.ServiceImpl.MediaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping
public class MediaController {
    @Autowired
    private MediaServiceImpl mediaService;


    public ResponseEntity<FlateDto> createFlate(@PathVariable int ownerId,
                                                @RequestParam String address,
                                                @RequestParam String rent,
                                                @RequestParam boolean available,
                                                @RequestParam("files") List<MultipartFile> files)  {
        FlateDto flateDto=mediaService.createFlate(ownerId,address,rent,available,files);
        return new ResponseEntity<>(flateDto, HttpStatus.OK);
    }
}
