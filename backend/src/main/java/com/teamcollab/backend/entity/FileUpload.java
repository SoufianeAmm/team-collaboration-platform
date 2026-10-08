package com.teamcollab.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "file_uploads")
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The randomized name actually used on disk (avoids collisions/overwrites).
    @Column(nullable = false)
    private String filename;

    // The name the user originally uploaded it as - what we show in the UI.
    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private Instant uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Optional: a file can belong to a specific task within the project, or just the project in general.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    protected FileUpload() {
    }

    public FileUpload(String filename, String originalFilename, String fileType, long fileSize,
                       User uploader, Project project, Task task) {
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploader = uploader;
        this.project = project;
        this.task = task;
        this.uploadDate = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public User getUploader() {
        return uploader;
    }

    public Project getProject() {
        return project;
    }

    public Task getTask() {
        return task;
    }
}
