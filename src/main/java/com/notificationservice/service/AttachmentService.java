package com.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class AttachmentService {

    @Value("${notification.attachments.max-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${notification.attachments.allowed-types:pdf,doc,docx,jpg,jpeg,png,txt}")
    private String[] allowedTypes;

    @Value("${notification.attachments.storage-path:./attachments}")
    private String storagePath;

    public String saveAttachment(MultipartFile file) throws IOException {
        validateFile(file);

        String fileId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String filename = fileId + (fileExtension != null ? "." + fileExtension : "");

        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        Path filePath = storageDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        log.info("Attachment saved: {} (original: {})", filename, originalFilename);
        return fileId;
    }

    public byte[] getAttachment(String fileId) throws IOException {
        Path filePath = findFilePath(fileId);
        if (filePath == null) {
            throw new IOException("File not found: " + fileId);
        }
        return Files.readAllBytes(filePath);
    }

    public void deleteAttachment(String fileId) throws IOException {
        Path filePath = findFilePath(fileId);
        if (filePath != null) {
            Files.deleteIfExists(filePath);
            log.info("Attachment deleted: {}", fileId);
        }
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IOException("File size exceeds maximum allowed size: " + maxFileSize);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        if (fileExtension == null || !isAllowedFileType(fileExtension)) {
            throw new IOException("File type not allowed: " + fileExtension);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedFileType(String fileExtension) {
        for (String allowedType : allowedTypes) {
            if (allowedType.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    private Path findFilePath(String fileId) throws IOException {
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            return null;
        }

        return Files.list(storageDir)
                .filter(path -> path.getFileName().toString().startsWith(fileId))
                .findFirst()
                .orElse(null);
    }

    public String getAttachmentInfo(String fileId) throws IOException {
        Path filePath = findFilePath(fileId);
        if (filePath == null) {
            return null;
        }

        return Files.getAttribute(filePath, "size") + " bytes";
    }
}
