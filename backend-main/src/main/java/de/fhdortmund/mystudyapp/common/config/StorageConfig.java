package de.fhdortmund.mystudyapp.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
public class StorageConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Avatars served at /uploads/avatars/**
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + storageProperties.getAvatarLocation() + "/avatars/");

        // Event media served at /uploads/events/**
        registry.addResourceHandler("/uploads/events/**")
                .addResourceLocations("file:" + storageProperties.getEventMediaLocation() + "/events/");
    }
}