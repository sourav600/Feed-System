package com.newsfeed.backend.media;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over where uploaded post images live.
 */
public interface FileStorageService {

    /**
     * Stores the file and returns a public URL (relative or absolute) clients can load the image
     * from directly.
     */
    String store(MultipartFile file);
}