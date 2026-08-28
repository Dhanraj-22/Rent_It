package com.example.rent_it.Controller;

import com.example.rent_it.DTO.PropertyDto;
import com.example.rent_it.Service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyDto> addProperty(
            @RequestPart("property") PropertyDto propertyDto,
            @RequestPart(value = "files", required = false) MultipartFile[] files) throws IOException {

        PropertyDto saved = propertyService.addProperty(propertyDto, files, propertyDto.getOwnerId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping(value = "/add/{ownerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyDto> addPropertyForOwner(
            @RequestPart("property") PropertyDto propertyDto,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @PathVariable Long ownerId) throws IOException {

        PropertyDto saved = propertyService.addProperty(propertyDto, files, ownerId);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PropertyDto>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDto> getProperty(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getProperty(id));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<PropertyDto>> getPropertyByCityContainingIgnoreCase(@PathVariable String city) {
        return ResponseEntity.ok(propertyService.getPropertyByCityContainingIgnoreCase(city));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyDto> updateProperty(
            @PathVariable Long id,
            @RequestPart("property") PropertyDto propertyDto,
            @RequestPart(value = "files", required = false) MultipartFile[] files) throws IOException {

        return ResponseEntity.ok(propertyService.updateProperty(id, propertyDto, files));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.ok("Property deleted successfully");
    }
}
