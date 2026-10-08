package com.teamcollab.backend.storage;

import com.teamcollab.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootDir;

    public LocalFileStorageService(@Value("${file.upload-directory}") String uploadDirectory) {
        this.rootDir = Path.of(uploadDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory: " + rootDir, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        // A random name on disk avoids collisions and path-traversal tricks from the original
        // filename - the user-facing name is kept separately as metadata (FileUpload.originalFilename).
        String extension = extensionOf(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        try {
            file.transferTo(rootDir.resolve(storedFilename));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
        return storedFilename;
    }

    @Override
    public Resource loadAsResource(String storedFilename) {
        Path file = rootDir.resolve(storedFilename).normalize();
        if (!file.startsWith(rootDir)) {
            // storedFilename always comes from our own store() output, but never trust a path blindly.
            throw new ResourceNotFoundException("File not found: " + storedFilename);
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + storedFilename);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + storedFilename);
        }
    }

    @Override
    public void delete(String storedFilename) {
        try {
            Files.deleteIfExists(rootDir.resolve(storedFilename).normalize());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file: " + storedFilename, e);
        }
    }

    private String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot + 1);
    }
}
