package com.notificationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AttachmentServiceTest {

    private AttachmentService attachmentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentService();
        ReflectionTestUtils.setField(attachmentService, "maxFileSize", 10485760L);
        ReflectionTestUtils.setField(attachmentService, "allowedTypes", new String[]{"pdf", "txt"});
        ReflectionTestUtils.setField(attachmentService, "storagePath", tempDir.toString());
    }

    @Test
    void saveAttachment_WithValidFile_ShouldReturnFileId() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        // Act
        String fileId = attachmentService.saveAttachment(file);

        // Assert
        assertNotNull(fileId);
        assertFalse(fileId.isEmpty());
    }

    @Test
    void saveAttachment_WithEmptyFile_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
        );

        // Act & Assert
        assertThrows(IOException.class, () -> {
            attachmentService.saveAttachment(file);
        });
    }
}
