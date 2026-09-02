package com.henheang.hphsar.mapper;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.model.file.StoredFile;
import com.henheang.hphsar.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-Postgres coverage for FileRepository, which uses annotation-based
 * MyBatis SQL (no separate XML mapper) since it is a single, joinless table.
 * Proves insert/find round-trips correctly and unknown ids return null.
 */
class FileRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private FileRepository fileRepository;

    private String uniqueId() {
        return UUID.randomUUID().toString();
    }

    @Test
    void insertFile_thenFindById_returnsTheStoredRowWithMatchingBytes() {
        String id = uniqueId();
        byte[] data = {1, 2, 3, 4};

        Integer rowsAffected = fileRepository.insertFile(id, "image/png", data);
        StoredFile found = fileRepository.findById(id);

        assertEquals(1, rowsAffected);
        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals("image/png", found.getContentType());
        assertArrayEquals(data, found.getData());
        assertNotNull(found.getCreatedDate());
    }

    @Test
    void findById_unknownId_returnsNull() {
        assertNull(fileRepository.findById(uniqueId()));
    }
}
