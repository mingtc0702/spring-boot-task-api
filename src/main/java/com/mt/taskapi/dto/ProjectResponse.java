package com.mt.taskapi.dto;

import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        List<TaskResponse> tasks
) {
}