package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.FileUpload;

import java.time.Instant;

public record FileUploadResponse(
        Long id,
        String originalFilename,
        String fileType,
        long fileSize,
        Instant uploadDate,
        UserResponse uploader,
        Long projectId,
        Long taskId
) {
    public static FileUploadResponse from(FileUpload file) {
        return new FileUploadResponse(
                file.getId(),
                file.getOriginalFilename(),
                file.getFileType(),
                file.getFileSize(),
                file.getUploadDate(),
                UserResponse.from(file.getUploader()),
                file.getProject().getId(),
                file.getTask() == null ? null : file.getTask().getId()
        );
    }
}
