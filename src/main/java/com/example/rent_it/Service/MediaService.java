package com.example.rent_it.Service;

import com.example.rent_it.Entitys.Flates;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {
    public Flates createFlate(int ownerId, String address, String rent, boolean available, List<MultipartFile> files);
}
