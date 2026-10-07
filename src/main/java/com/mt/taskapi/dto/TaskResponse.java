package com.mt.taskapi.dto;

import com.mt.taskapi.model.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status
) {
}
