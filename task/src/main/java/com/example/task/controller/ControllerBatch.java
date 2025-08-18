package com.example.task.controller;

import com.example.task.Service.BatchSer;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/batch")
@CrossOrigin(origins = "http://localhost:3000") // Add CORS if needed
public class ControllerBatch {
    private final BatchSer batchJobService;

    public ControllerBatch(BatchSer batchJobService) {
        this.batchJobService = batchJobService;
    }

    @PostMapping(
            path = "/start",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> startBatch(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded (param name must be 'file').");
        }
        try {
            Path tempFile = Files.createTempFile("batch_upload_", ".csv");
            file.transferTo(tempFile.toFile());
            String msg = batchJobService.startJob(tempFile.toAbsolutePath().toString());
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to start batch job: " + e.getMessage());
        }
    }

    @GetMapping("/export-customers")
    public ResponseEntity<Resource> exportCustomers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Integer ratings,
            @RequestParam(required = false) String feedback) throws IOException {


            // Create filters map
            Map<String, Object> filters = new HashMap<>();
            if (firstName != null && !firstName.trim().isEmpty()) {
                filters.put("firstName", firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                filters.put("lastName", lastName.trim());
            }
            if (ratings != null) {
                filters.put("ratings", ratings);
            }
            if (feedback != null && !feedback.trim().isEmpty()) {
                filters.put("feedback", feedback.trim());
            }

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "customers_export_" + timestamp + ".csv";

            // Create temporary file
            Path tempFile = Files.createTempFile("customer_export_", ".csv");

            // Use batch processing to generate CSV
            String result = batchJobService.startExportJob(tempFile.toString(), filters);

            // Create resource from file
            FileSystemResource resource = new FileSystemResource(tempFile.toFile());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(((FileSystemResource) resource).contentLength()))
                    .body((Resource) resource);

    }
}

