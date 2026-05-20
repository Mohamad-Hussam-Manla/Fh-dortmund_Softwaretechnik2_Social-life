package de.fhdortmund.mystudyapp.common.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Slf4j
@Service
public class ThumbnailService {

    private static final int THUMB_WIDTH = 400;
    private static final int THUMB_HEIGHT = 300;
    private static final int MEDIUM_WIDTH = 800;
    private static final int MEDIUM_HEIGHT = 600;

    /**
     * Generates thumbnail and medium versions of an image.
     *
     * @param originalPath Path to the original uploaded image
     * @return String[2] where [0] = thumbnail URL, [1] = medium URL
     */
    public String[] generateThumbnails(Path originalPath, String baseUrl) {
        String originalFilename = originalPath.getFileName().toString();
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        Path parentDir = originalPath.getParent();

        String thumbFilename = baseName + "_thumb." + extension;
        String mediumFilename = baseName + "_medium." + extension;

        Path thumbPath = parentDir.resolve(thumbFilename);
        Path mediumPath = parentDir.resolve(mediumFilename);

        try {
            // Thumbnail: 400x300, crop to fit
            Thumbnails.of(originalPath.toFile())
                    .size(THUMB_WIDTH, THUMB_HEIGHT)
                    .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                    .outputQuality(0.85)
                    .toFile(thumbPath.toFile());

            // Medium: 800x600, preserve aspect ratio
            Thumbnails.of(originalPath.toFile())
                    .size(MEDIUM_WIDTH, MEDIUM_HEIGHT)
                    .outputQuality(0.90)
                    .toFile(mediumPath.toFile());

            String relativeThumb = baseUrl.replace("/uploads/", "") + "_thumb." + extension;
            String relativeMedium = baseUrl.replace("/uploads/", "") + "_medium." + extension;

            return new String[]{
                    "/uploads/" + relativeThumb,
                    "/uploads/" + relativeMedium
            };

        } catch (IOException e) {
            log.error("Failed to generate thumbnails for {}", originalPath, e);
            // Return original URL for both if generation fails
            return new String[]{baseUrl, baseUrl};
        }
    }

    /**
     * Deletes thumbnail and medium variants alongside the original.
     */
    public void deleteThumbnails(Path originalPath) {
        String originalFilename = originalPath.getFileName().toString();
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        Path parentDir = originalPath.getParent();

        Path thumbPath = parentDir.resolve(baseName + "_thumb." + extension);
        Path mediumPath = parentDir.resolve(baseName + "_medium." + extension);

        try {
            Files.deleteIfExists(thumbPath);
            Files.deleteIfExists(mediumPath);
        } catch (IOException e) {
            log.warn("Could not delete thumbnails for {}", originalPath, e);
        }
    }
}