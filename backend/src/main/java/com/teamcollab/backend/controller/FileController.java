package com.teamcollab.backend.controller;

import com.teamcollab.backend.dto.FileUploadResponse;
import com.teamcollab.backend.entity.FileUpload;
import com.teamcollab.backend.security.UserPrincipal;
import com.teamcollab.backend.service.FileUploadService;
import com.teamcollab.backend.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class FileController {

    // Avatars aren't tracked as FileUpload metadata (no project/uploader bookkeeping needed),
    // so content type is inferred from the stored extension rather than looked up from a record.
    private static final Map<String, String> IMAGE_CONTENT_TYPES = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "gif", "image/gif",
            "webp", "image/webp"
    );

    private final FileUploadService fileUploadService;
    private final FileStorageService fileStorageService;

    public FileController(FileUploadService fileUploadService, FileStorageService fileStorageService) {
        this.fileUploadService = fileUploadService;
        this.fileStorageService = fileStorageService;
    }

    // Public and unauthenticated on purpose: <img> tags can't attach an Authorization header,
    // and a profile picture isn't sensitive enough to need one.
    @GetMapping("/api/avatars/{filename}")
    public ResponseEntity<Resource> avatar(@PathVariable String filename) {
        Resource resource = fileStorageService.loadAsResource(filename);
        String extension = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        String contentType = IMAGE_CONTENT_TYPES.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(resource);
    }

    @GetMapping("/api/projects/{projectId}/files")
    public List<FileUploadResponse> listForProject(@PathVariable Long projectId,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return fileUploadService.listForProject(projectId, principal.getUser())
                .stream().map(FileUploadResponse::from).toList();
    }

    @PostMapping(value = "/api/projects/{projectId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FileUploadResponse upload(@PathVariable Long projectId,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam(required = false) Long taskId,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        FileUpload saved = fileUploadService.upload(projectId, taskId, file, principal.getUser());
        return FileUploadResponse.from(saved);
    }

    @GetMapping("/api/files/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        FileUpload file = fileUploadService.getMetadata(id, principal.getUser());
        Resource resource = fileUploadService.loadFileContent(file);

        String contentType = file.getFileType() != null ? file.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @DeleteMapping("/api/files/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        fileUploadService.delete(id, principal.getUser());
    }
}
