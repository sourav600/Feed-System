package com.newsfeed.backend.media;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LocalDiskFileStorageService implements FileStorageService {

    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp");

    private final FileStorageProperties properties;

    @PostConstruct
    void ensureStorageDirectoryExists() {
        try {
            Files.createDirectories(Path.of(properties.storagePath()));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create media storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty.");
        }
        String extension = ALLOWED_IMAGE_TYPES.get(file.getContentType());
        if (extension == null || !isAllowedExtension(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported image type. Allowed: " + Set.copyOf(ALLOWED_IMAGE_TYPES.keySet()));
        }

        // Server-generated filename only - the client-supplied original filename is never used to
        // build a path, which rules out path traversal / filename-injection entirely.
        String filename = UUID.randomUUID() + extension;
        Path target = Path.of(properties.storagePath()).resolve(filename).normalize();

        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded image", e);
        }

        return properties.publicUrlPrefix() + "/" + filename;
    }

    private static boolean isAllowedExtension(String extension) {
        return ALLOWED_IMAGE_TYPES.containsValue(extension);
    }
}
