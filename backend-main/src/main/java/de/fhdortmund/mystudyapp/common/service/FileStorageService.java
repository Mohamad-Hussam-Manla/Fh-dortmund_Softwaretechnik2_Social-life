package de.fhdortmund.mystudyapp.common.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import de.fhdortmund.mystudyapp.common.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles all file I/O: avatar uploads and event media (images + videos).
 *
 * NOTE: If you already have avatar-upload methods in this class, keep them and
 * simply append the "Event Media" section below. Only the new methods and the
 * two new private helpers are additions; the avatar helpers remain unchanged.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageProperties storageProperties;

    /* ==================== Avatar (existing — keep as-is) ==================== */

    /**
     * Store an avatar file and return its public URL.
     * Adjust the body to match whatever your original implementation does.
     */
    public String storeAvatar(MultipartFile file, UUID userId) {
        String ext = getFileExtension(file.getOriginalFilename());
        if (!isValidImageType(ext)) {
            throw new IllegalArgumentException("Only JPG, PNG, and WEBP avatars are allowed");
        }
        String filename = "avatar_" + userId + "." + ext;
        Path uploadPath = Paths.get(storageProperties.getAvatarLocation()).resolve("avatars");
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/avatars/" + filename;
        } catch (IOException ex) {
            log.error("Could not store avatar for user {}", userId, ex);
            throw new RuntimeException("Could not store avatar. Please try again later.");
        }
    }

    /**
     * Deletes a previously stored avatar file from disk.
     * Called by UserService when a user uploads a new avatar or deletes their account.
     */
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) return;
        try {
            String relativePath = avatarUrl.replace("/uploads/", "");
            Path filePath = Paths.get(storageProperties.getAvatarLocation()).resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.warn("Could not delete avatar at URL '{}': {}", avatarUrl, ex.getMessage());
        }
    }

    /* ==================== Event Media (new) ==================== */

    /**
     * Validates size/type and stores an image for an event.
     *
     * @return public URL of the stored image
     */
    public String storeEventImage(MultipartFile file, UUID eventId) {
        validateFileSize(file, storageProperties.getMaxImageSizeBytes(), "Image");
        String ext = getFileExtension(file.getOriginalFilename());
        if (!isValidImageType(ext)) {
            throw new IllegalArgumentException("Only JPG, PNG, and WEBP images are allowed");
        }
        return storeEventFile(file, eventId, "images", ext);
    }

    /**
     * Validates size/type and stores a video for an event.
     *
     * @return public URL of the stored video
     */
    public String storeEventVideo(MultipartFile file, UUID eventId) {
        validateFileSize(file, storageProperties.getMaxVideoSizeBytes(), "Video");
        String ext = getFileExtension(file.getOriginalFilename());
        if (!isValidVideoType(ext)) {
            throw new IllegalArgumentException("Only MP4, WEBM, and MOV videos are allowed");
        }
        return storeEventFile(file, eventId, "videos", ext);
    }

    /**
     * Deletes a previously stored event-media file from disk.
     * Silently skips if the URL is blank or the file is already gone.
     */
    public void deleteEventMedia(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            // Strip the leading "/uploads/" prefix to get the relative path
            String relativePath = fileUrl.replace("/uploads/", "");
            Path filePath = Paths.get(storageProperties.getEventMediaLocation()).resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.warn("Could not delete event media at URL '{}': {}", fileUrl, ex.getMessage());
        }
    }

    /* ==================== Private Helpers ==================== */

    private String storeEventFile(MultipartFile file, UUID eventId, String subfolder, String extension) {
        String newFilename = UUID.randomUUID() + "." + extension;
        Path uploadPath = Paths.get(storageProperties.getEventMediaLocation())
                .resolve("events")
                .resolve(eventId.toString())
                .resolve(subfolder);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path targetLocation = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/events/" + eventId + "/" + subfolder + "/" + newFilename;
        } catch (IOException ex) {
            log.error("Could not store event file '{}' for event {}", newFilename, eventId, ex);
            throw new RuntimeException("Could not store file. Please try again later.");
        }
    }

    private void validateFileSize(MultipartFile file, long maxBytes, String type) {
        if (file.getSize() > maxBytes) {
            long maxMb = maxBytes / (1024 * 1024);
            throw new IllegalArgumentException(type + " exceeds maximum allowed size of " + maxMb + " MB");
        }
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isValidImageType(String extension) {
        return extension != null
                && (extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("png")
                || extension.equals("webp"));
    }

    private boolean isValidVideoType(String extension) {
        return extension != null
                && (extension.equals("mp4")
                || extension.equals("webm")
                || extension.equals("mov")
                || extension.equals("quicktime"));
    }
}