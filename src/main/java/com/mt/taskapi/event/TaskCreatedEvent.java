package com.mt.taskapi.event;

import java.time.Instant;
import java.util.UUID;

public record TaskCreatedEvent(
        UUID eventId,
        String eventType,
        Long taskId,
        Long projectId,
        Instant occurredAt
) {
}
