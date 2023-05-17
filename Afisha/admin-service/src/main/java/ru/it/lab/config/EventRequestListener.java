package ru.it.lab.config;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.it.lab.dao.EventApprovalRequestDao;
import ru.it.lab.dto.EventRequestDTO;
import ru.it.lab.entities.EventApprovalRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Component
public class EventRequestListener {
    @Autowired
    private EventApprovalRequestDao requestDao;

    @RabbitListener(queues = MQConfig.QUEUE_EVENT)
    public void listener(EventRequestDTO eventRequestDTO) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(eventRequestDTO.getCreation_time()),
                TimeZone.getDefault().toZoneId());
        try {
            EventApprovalRequest eventApprovalRequest = new EventApprovalRequest();
            eventApprovalRequest.setEventId(eventRequestDTO.getEventId());
            eventApprovalRequest.setCreationTime(time);
            eventApprovalRequest.setOrganizerId(eventRequestDTO.getOrganizerId());
            requestDao.create(eventApprovalRequest);
        } catch (RuntimeException ignored) {

        }

    }
}
