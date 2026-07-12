package com.newsfeed.backend.media;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

    private final FileStorageProperties properties;

    // Filenames are always UUID-generated, never derived from client input - avoids path
    // traversal / collisions from the original filename entirely.
    public String store(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PNG, JPEG, GIF, or WEBP images are allowed.");
        }

        String filename = UUID.randomUUID() + extensionFor(contentType);

        try {
            Path storageDir = Path.of(properties.storagePath());
            Files.createDirectories(storageDir);
            file.transferTo(storageDir.resolve(filename));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded file.", e);
        }

        return properties.publicUrlPrefix() + "/" + filename;
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}