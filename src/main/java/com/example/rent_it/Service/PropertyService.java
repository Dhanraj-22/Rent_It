package com.example.rent_it.Service;

import com.example.rent_it.DTO.PropertyDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PropertyService {

    PropertyDto addProperty(PropertyDto propertyDto,
                            MultipartFile[] files,
                            Long ownerId) throws IOException;

    PropertyDto getProperty(Long id);

    List<PropertyDto> getAllProperties();

    PropertyDto updateProperty(Long id,
                               PropertyDto propertyDto,
                               MultipartFile[] files) throws IOException;

    void deleteProperty(Long id);
    List<PropertyDto> getPropertyByCityContainingIgnoreCase(String city);
}
