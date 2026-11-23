package com.mt.taskapi.messaging;

import com.mt.taskapi.event.TaskCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TaskEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(TaskEventConsumer.class);

    @KafkaListener(topics = "task-events", groupId = "task-api-group")
    public void consumeTaskCreated(TaskCreatedEvent event) {

        log.info(
                "Consumed TaskCreatedEvent: eventId={}, taskId={}, projectId={}",
                event.eventId(),
                event.taskId(),
                event.projectId()
        );
    }
}
