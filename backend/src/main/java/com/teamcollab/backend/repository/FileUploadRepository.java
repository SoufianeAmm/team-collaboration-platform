package com.teamcollab.backend.repository;

import com.teamcollab.backend.entity.FileUpload;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    List<FileUpload> findByProject(Project project);
    List<FileUpload> findByTask(Task task);
    List<FileUpload> findByUploader(User uploader);
}
