package dev.right.filez.services;

import dev.right.filez.Application;
import jakarta.annotation.Nullable;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

// Adapted version of https://www.youtube.com/watch?v=y2oXmWvd5zQ

@Service
public class FileUploaderService {
    private final Path rootPath;

    public FileUploaderService() {
        this.rootPath = getStorageAbsolutePath();
    }

    private Path getStorageAbsolutePath() {
        String filesAbsoluteStoragePath = Application.fileStoragePaths;
        if (filesAbsoluteStoragePath != null) {
            if (!filesAbsoluteStoragePath.equalsIgnoreCase("default")) {
                return Paths.get(filesAbsoluteStoragePath);
            }
        }

        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")) {
            return Paths.get("C:\\FileZ\\");
        } else {
            return Paths.get("/var/FileZ/"); // Assumes you're on Mac or Linux.
        }
    }

    public String storeFile(InputStream inputStream, String originalName) throws IOException {
        LocalDate today = LocalDate.now();
        Path dateDir = rootPath
                .resolve(
                        today.getYear() + File.separator +
                                String.format("%02d", today.getDayOfMonth()) + File.separator +
                                String.format("%02d", today.getDayOfMonth())
                );

        Files.createDirectories(dateDir);

        String ext = getFileExtension(originalName);
        String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path filePath = dateDir.resolve(storedName);

        try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
            StreamUtils.copy(inputStream, outputStream);
        }

        return rootPath.resolve(filePath).toString();
    }

    public UrlResource getFileResource(String stored) throws IOException {
        Path filePath = rootPath.resolve(stored).normalize().toAbsolutePath();
        Path normalizedRoot = rootPath.normalize().toAbsolutePath();

        if (!filePath.startsWith(normalizedRoot)) {
            throw new SecurityException("Access denied.");
        }

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found.");
        }

        return new UrlResource(filePath.toUri());
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return lastDot == -1 ? "" : fileName.substring(lastDot+1);
    }
}
