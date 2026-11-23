package com.mt.taskapi.messaging;

import com.mt.taskapi.event.TaskCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TaskCreatedEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(TaskCreatedEventListener.class);

    private final TaskEventProducer taskEventProducer;

    public TaskCreatedEventListener(TaskEventProducer taskEventProducer) {
        this.taskEventProducer = taskEventProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {

        log.info(
                "Database committed. Publishing TaskCreatedEvent: eventId={}, taskId={}",
                event.eventId(),
                event.taskId()
        );

        taskEventProducer.publishTaskCreated(event);
    }
}
