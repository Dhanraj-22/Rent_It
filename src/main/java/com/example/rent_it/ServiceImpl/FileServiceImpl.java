package com.example.rent_it.ServiceImpl;

import com.example.rent_it.Service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            name = "upload.bin";
        }
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");

        String randomId = UUID.randomUUID().toString();

        String fileName = randomId + "_" + name;

        Path uploadRoot = Paths.get(path).toAbsolutePath().normalize();
        File folder = uploadRoot.toFile();

        if(!folder.exists()){
            folder.mkdirs();
        }

        Path filePath = uploadRoot.resolve(fileName).normalize();
        if (!filePath.startsWith(uploadRoot)) {
            throw new IOException("Invalid file path");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}
