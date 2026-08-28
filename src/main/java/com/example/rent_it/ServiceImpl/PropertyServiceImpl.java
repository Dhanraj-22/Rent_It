package com.example.rent_it.ServiceImpl;

import com.example.rent_it.DTO.PropertyDto;
import com.example.rent_it.Entity.Property;
import com.example.rent_it.Entity.PropertyImage;
import com.example.rent_it.Entity.Role;
import com.example.rent_it.Entity.User;
import com.example.rent_it.Exception.ResourceNotFoundException;
import com.example.rent_it.Repository.PropertyRepository;
import com.example.rent_it.Repository.UserRepositoty;
import com.example.rent_it.Service.FileService;
import com.example.rent_it.Service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final UserRepositoty userRepositoty;
    private final FileService fileService;

    @Value("${property.image.path:uploads}")
    private String path;

    @Value("${app.base-url:http://localhost:8086}")
    private String baseUrl;

    @Override
    public PropertyDto addProperty(PropertyDto propertyDto, MultipartFile[] files, Long ownerId) throws IOException {
        Property property = new Property();
        copyPropertyFields(property, propertyDto);
        property.setAvailable(propertyDto.getAvailable() == null || propertyDto.getAvailable());
        property.setImages(new ArrayList<>());

        Long resolvedOwnerId = ownerId != null ? ownerId : propertyDto.getOwnerId();
        if (resolvedOwnerId != null) {
            property.setOwner(findOwner(resolvedOwnerId));
        }

        addImages(property, files);

        return toDto(propertyRepository.save(property));
    }

    @Override
    public PropertyDto getProperty(Long id) {
        return toDto(findProperty(id));
    }

    @Override
    public List<PropertyDto> getAllProperties() {
        return propertyRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PropertyDto updateProperty(Long id, PropertyDto propertyDto, MultipartFile[] files) throws IOException {
        Property property = findProperty(id);
        copyPropertyFields(property, propertyDto);

        Long ownerId = propertyDto.getOwnerId();
        if (ownerId != null) {
            property.setOwner(findOwner(ownerId));
        }

        if (hasFiles(files)) {
            if (property.getImages() == null) {
                property.setImages(new ArrayList<>());
            } else {
                property.getImages().clear();
            }
            addImages(property, files);
        }

        return toDto(propertyRepository.save(property));
    }

    @Override
    public void deleteProperty(Long id) {
        Property property = findProperty(id);
        propertyRepository.delete(property);
    }

    @Override
    public List<PropertyDto> getPropertyByCityContainingIgnoreCase(String city) {
        return propertyRepository.findByCityContainingIgnoreCase(city)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Property findProperty(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id = " + id));
    }

    private User findOwner(Long ownerId) {
        User owner = userRepositoty.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id = " + ownerId));

        if (owner.getRole() != Role.OWNER && owner.getRole() != Role.ADMIN) {
            throw new ResourceNotFoundException("Only OWNER users can add rental properties");
        }

        return owner;
    }

    private void copyPropertyFields(Property property, PropertyDto propertyDto) {
        if (propertyDto.getTitle() != null) {
            property.setTitle(propertyDto.getTitle());
        }
        if (propertyDto.getDescription() != null) {
            property.setDescription(propertyDto.getDescription());
        }
        if (propertyDto.getAddress() != null) {
            property.setAddress(propertyDto.getAddress());
        }
        if (propertyDto.getRent() != null) {
            property.setRent(propertyDto.getRent());
        }
        if (propertyDto.getDeposit() != null) {
            property.setDeposit(propertyDto.getDeposit());
        }
        if (propertyDto.getCity() != null) {
            property.setCity(propertyDto.getCity());
        }
        if (propertyDto.getBhkType() != null) {
            property.setBhkType(propertyDto.getBhkType());
        }
        if (propertyDto.getAvailable() != null) {
            property.setAvailable(propertyDto.getAvailable());
        }
    }

    private void addImages(Property property, MultipartFile[] files) throws IOException {
        if (files == null) {
            return;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String fileName = fileService.uploadImage(path, file);
            PropertyImage image = new PropertyImage();
            image.setImageUrl(fileName);
            image.setProperty(property);
            property.getImages().add(image);
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

    private PropertyDto toDto(Property property) {
        PropertyDto dto = new PropertyDto();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setAddress(property.getAddress());
        dto.setRent(property.getRent());
        dto.setDeposit(property.getDeposit());
        dto.setCity(property.getCity());
        dto.setBhkType(property.getBhkType());
        dto.setAvailable(property.getAvailable());

        if (property.getOwner() != null) {
            dto.setOwnerId(property.getOwner().getId());
            dto.setOwnerName(property.getOwner().getName());
        }

        dto.setImages(property.getImages() == null
                ? List.of()
                : property.getImages()
                        .stream()
                        .map(PropertyImage::getImageUrl)
                        .map(this::imageUrl)
                        .toList());

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
