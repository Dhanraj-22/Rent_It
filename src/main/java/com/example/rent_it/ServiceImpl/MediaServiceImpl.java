package com.example.rent_it.ServiceImpl;

import com.example.rent_it.Dto.MediaDto;
import com.example.rent_it.Dto.OwnerDto;
import com.example.rent_it.Entitys.Flates;
import com.example.rent_it.Entitys.Media;
import com.example.rent_it.Entitys.Owner;
import com.example.rent_it.Service.MediaService;
import com.example.rent_it.reposactory.FlatesRepo;
import com.example.rent_it.reposactory.MediaRepo;
import com.example.rent_it.reposactory.OwnerRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
@Service
public class MediaServiceImpl implements MediaService {
    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private MediaRepo mediaRepo;
    @Autowired
    private FlatesRepo flatesRepo;
    @Autowired
    private OwnerRepo ownerRepo;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Flates createFlate(int ownerId, String address, String rent, boolean available, List<MultipartFile> files) {
        Owner owner = ownerRepo.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Flates flates = new Flates();
        flates.setAddress(address);
        flates.setRent(rent);
        flates.setAvailable(available);
        flates.setFlateOwner(owner.getOwnerName());
        flates = flatesRepo.save(flates);

        List<Media> mediaList = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir, fileName);

            try {
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e);
            }

            Media media = new Media();
            media.setFileName(fileName);
            media.setFileType(file.getContentType());
            media.setFileUrl("/uploads/" + fileName); // relative path for frontend
            media.setFlate(flates);

            mediaList.add(mediaRepo.save(media));
        }

        flates.setMediaFiles(mediaList);
        return flates;
    }
}

