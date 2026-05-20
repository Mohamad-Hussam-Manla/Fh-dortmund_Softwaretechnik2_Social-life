package de.fhdortmund.mystudyapp.events.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BulkEventActionResult {
    private int processedCount;
    private int successCount;
    private int failedCount;
    private List<UUID> succeededIds;
    private List<UUID> failedIds;
    private String message;
}