package com.henheang.hphsar.service;

import com.henheang.hphsar.model.file.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    /** Stores the upload in tb_file and returns the generated file id (used to build the retrieval URL). */
    String store(MultipartFile file) throws IOException;

    /** @throws com.henheang.hphsar.exception.NotFoundException if no file exists with this id */
    StoredFile getById(String id);
}
