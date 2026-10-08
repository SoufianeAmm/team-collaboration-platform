package com.teamcollab.backend.service;

import com.teamcollab.backend.entity.FileUpload;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.FileTooLargeException;
import com.teamcollab.backend.exception.InvalidFileTypeException;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.FileUploadRepository;
import com.teamcollab.backend.repository.ProjectRepository;
import com.teamcollab.backend.repository.TaskRepository;
import com.teamcollab.backend.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class FileUploadService {

    // Matches the file types called out in the spec: PDF, PNG, JPG, DOCX, ZIP (+ jpeg as a natural variant).
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "docx", "zip");

    private final FileUploadRepository fileUploadRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final FileStorageService fileStorageService;
    private final ProjectAccessPolicy accessPolicy;
    private final long maxSizeBytes;

    public FileUploadService(FileUploadRepository fileUploadRepository, ProjectRepository projectRepository,
                              TaskRepository taskRepository, FileStorageService fileStorageService,
                              ProjectAccessPolicy accessPolicy, @Value("${file.max-size-mb}") long maxSizeMb) {
        this.fileUploadRepository = fileUploadRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.fileStorageService = fileStorageService;
        this.accessPolicy = accessPolicy;
        this.maxSizeBytes = maxSizeMb * 1024 * 1024;
    }

    public FileUpload upload(Long projectId, Long taskId, MultipartFile file, User uploader) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertMember(project, uploader);
        validate(file);

        Task task = null;
        if (taskId != null) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task " + taskId + " not found"));
        }

        String storedFilename = fileStorageService.store(file);
        FileUpload entity = new FileUpload(
                storedFilename,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                uploader,
                project,
                task
        );
        return fileUploadRepository.save(entity);
    }

    public List<FileUpload> listForProject(Long projectId, User requester) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertMember(project, requester);
        return fileUploadRepository.findByProject(project);
    }

    public FileUpload getMetadata(Long fileId, User requester) {
        FileUpload file = findFileOrThrow(fileId);
        accessPolicy.assertMember(file.getProject(), requester);
        return file;
    }

    public Resource loadFileContent(FileUpload file) {
        return fileStorageService.loadAsResource(file.getFilename());
    }

    public void delete(Long fileId, User requester) {
        FileUpload file = findFileOrThrow(fileId);
        boolean isUploader = file.getUploader().getId().equals(requester.getId());
        if (!isUploader && !accessPolicy.isOwnerOrAdmin(file.getProject(), requester)) {
            throw new AccessDeniedException("Only the uploader, the project owner, or an admin can delete this file");
        }
        fileStorageService.delete(file.getFilename());
        fileUploadRepository.delete(file);
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileTypeException("Cannot upload an empty file");
        }
        String extension = extensionOf(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileTypeException(
                    "File type '." + extension + "' is not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }
        if (file.getSize() > maxSizeBytes) {
            throw new FileTooLargeException(
                    "File exceeds the maximum allowed size of " + (maxSizeBytes / 1024 / 1024) + " MB");
        }
    }

    private String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot + 1);
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " not found"));
    }

    private FileUpload findFileOrThrow(Long id) {
        return fileUploadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File " + id + " not found"));
    }
}
