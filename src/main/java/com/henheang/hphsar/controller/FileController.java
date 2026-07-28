package com.henheang.hphsar.controller;

import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.model.file.StoredFile;
import com.henheang.hphsar.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController extends BaseController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String id = fileService.store(file);

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/files/")
                .path(id)
                .toUriString();

        return ok("File uploaded successfully.", fileUrl);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        StoredFile storedFile = fileService.getById(id);

        MediaType mediaType = (storedFile.getContentType() != null)
                ? MediaType.parseMediaType(storedFile.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .body(storedFile.getData());
    }
}
