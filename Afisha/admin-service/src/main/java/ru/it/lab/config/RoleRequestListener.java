package ru.it.lab.config;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.it.lab.dao.RoleRequestDao;
import ru.it.lab.dto.RoleRequestDTO;
import ru.it.lab.entities.RoleRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Component
public class RoleRequestListener {
    @Autowired
    private RoleRequestDao requestDao;

    @RabbitListener(queues = MQConfig.QUEUE_ROLE)
    public void listener(RoleRequestDTO requestDTO) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(requestDTO.getCreation_time()),
                TimeZone.getDefault().toZoneId());
        try {
            requestDao.create(new RoleRequest(requestDTO.getUsername(), time, requestDTO.getRoleId()));
        } catch (RuntimeException ignored) {

        }

    }
}
