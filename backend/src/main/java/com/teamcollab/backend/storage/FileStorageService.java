package com.teamcollab.backend.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over "where do uploaded file bytes actually live." Today that's the local
 * {@code /uploads} folder (see {@link LocalFileStorageService}); the rest of the app only talks
 * to this interface, so swapping in an S3-backed implementation later touches one class, not
 * every controller/service that handles files.
 */
public interface FileStorageService {

    /** Writes the file to storage and returns the generated name it was stored under. */
    String store(MultipartFile file);

    Resource loadAsResource(String storedFilename);

    void delete(String storedFilename);
}
