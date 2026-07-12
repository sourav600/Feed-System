package com.newsfeed.backend.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media")
public record FileStorageProperties(String storagePath, String publicUrlPrefix) {
}