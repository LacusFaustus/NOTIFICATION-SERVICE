package com.notificationservice.controller;

import com.notificationservice.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attachments", description = "API for managing email attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    @Operation(summary = "Upload an attachment")
    public ResponseEntity<Map<String, String>> uploadAttachment(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = attachmentService.saveAttachment(file);

            Map<String, String> response = new HashMap<>();
            response.put("fileId", fileId);
            response.put("message", "File uploaded successfully");
            response.put("originalFilename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to upload attachment: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Download an attachment")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable String fileId) {
        try {
            byte[] fileContent = attachmentService.getAttachment(fileId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileId);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (IOException e) {
            log.error("Failed to download attachment {}: {}", fileId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<Map<String, String>> deleteAttachment(@PathVariable String fileId) {
        try {
            attachmentService.deleteAttachment(fileId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            response.put("fileId", fileId);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to delete attachment {}: {}", fileId, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/{fileId}/info")
    @Operation(summary = "Get attachment information")
    public ResponseEntity<Map<String, String>> getAttachmentInfo(@PathVariable String fileId) {
        try {
            String info = attachmentService.getAttachmentInfo(fileId);

            Map<String, String> response = new HashMap<>();
            response.put("fileId", fileId);
            response.put("info", info != null ? info : "File not found");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to get attachment info {}: {}", fileId, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get file info: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
