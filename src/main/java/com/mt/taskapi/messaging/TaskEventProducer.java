package com.mt.taskapi.messaging;

import com.mt.taskapi.event.TaskCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(TaskEventProducer.class);

    private static final String TOPIC = "task-events";

    private final KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate;

    public TaskEventProducer(
            KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTaskCreated(TaskCreatedEvent event) {

        String key = event.projectId().toString();

        kafkaTemplate.send(TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "Failed to publish TaskCreatedEvent: {}",
                                event.eventId(),
                                ex
                        );
                    } else {
                        log.info(
                                "Published TaskCreatedEvent: eventId={}, taskId={}, partition={}, offset={}",
                                event.eventId(),
                                event.taskId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
}

