package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.file.StoredFile;
import com.henheang.hphsar.repository.FileRepository;
import com.henheang.hphsar.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    public String store(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String id = UUID.randomUUID() + extension;

        fileRepository.insertFile(id, file.getContentType(), file.getBytes());
        return id;
    }

    @Override
    public StoredFile getById(String id) {
        StoredFile storedFile = fileRepository.findById(id);
        if (storedFile == null) {
            throw new NotFoundException("File not found: " + id);
        }
        return storedFile;
    }
}
