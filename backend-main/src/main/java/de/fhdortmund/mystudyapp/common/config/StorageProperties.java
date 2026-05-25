package de.fhdortmund.mystudyapp.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** Base directory for avatar uploads */
    private String avatarLocation = "uploads";

    /** Base directory for event media uploads */
    private String eventMediaLocation = "uploads";

    /** Maximum allowed size for a single image file (default 5 MB) */
    private long maxImageSizeBytes = 5L * 1024 * 1024;

    /** Maximum allowed size for a single video file (default 20 MB) */
    private long maxVideoSizeBytes = 20L * 1024 * 1024;

    /** Maximum number of images allowed per event */
    private int maxImagesPerEvent = 5;

    /** Maximum number of videos allowed per event */
    private int maxVideosPerEvent = 2;
}