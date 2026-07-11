package com.newsfeed.backend.config;

import com.newsfeed.backend.media.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + fileStorageProperties.storagePath().replaceAll("/+$", "") + "/";
        registry.addResourceHandler(fileStorageProperties.publicUrlPrefix() + "/**")
                .addResourceLocations(location);
    }
}
