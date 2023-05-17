package ru.it.lab.controller;

import com.google.protobuf.Int32Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.it.lab.Empty;
import ru.it.lab.EventProto;
import ru.it.lab.EventServiceGrpc;
import ru.it.lab.Id;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.dto.EventDTO;
import ru.it.lab.enums.EventType;

import java.time.ZoneId;

@RestController
@RequestMapping("/events")
public class EventsController {

    @GrpcClient("grpc-events-service")
    private EventServiceGrpc.EventServiceBlockingStub eventService;

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    ///////////////////////////////////

    @GetMapping("/{eventId}")
    public String getEventById(@PathVariable Long eventId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(eventService.getEventById(Id.newBuilder().setId(eventId).build()));
    }

    @GetMapping("/my_created_events")
    @PreAuthorize("hasAuthority('CREATING_ACTIONS')")
    public String getMyCreatedEvents() throws InvalidProtocolBufferException {
        UserProto user = userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());
        return JsonFormat.printer().print(eventService.getMyCreatedEvents(Id.newBuilder().setId(user.getId()).build()));
    }

    @GetMapping("/all")
    public String getApprovedEvents() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(eventService.getAllApprovedEvents(Empty.newBuilder().build()));
    }

    @GetMapping("/type")
    public String getApprovedEventsByType(@RequestParam String type) throws InvalidProtocolBufferException {
        EventType eventType = null;
        try {
            eventType = EventType.valueOf(type);
        } catch (RuntimeException e) {
            eventType = EventType.OTHER;
        }
        return JsonFormat.printer().print(eventService.getApprovedEventsByType(EventProto.newBuilder().setEventType(eventType.name()).build()));
    }

    @PostMapping("/my_created_events")
    @PreAuthorize("hasAuthority('CREATING_ACTIONS')")
    public String createEvent(@RequestBody EventDTO eventDTO) throws InvalidProtocolBufferException {
        EventType eventType = null;
        try {
            eventType = EventType.valueOf(eventDTO.getType());
        } catch (RuntimeException e) {
            eventType = EventType.OTHER;
        }
        UserProto user = userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());

        return JsonFormat.printer().print(eventService.createEvent(EventProto.newBuilder()
                .setOrganizerId(user.getId())
                .setEventType(eventType.name())
                .setName(eventDTO.getName())
                .setInfo(eventDTO.getInfo())
                .setPrice(Int32Value.newBuilder().setValue(eventDTO.getPrice()).build())
                .setStartTime(eventDTO.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond())
                .setLocation(eventDTO.getLocation())
                .build()));
    }

    @PostMapping("/my_created_events/{eventId}/update")
    @PreAuthorize("hasAuthority('CREATING_ACTIONS')")
    public String updateEvent(@PathVariable Long eventId, @RequestBody EventDTO eventDTO) throws InvalidProtocolBufferException {
        UserProto user = userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());
        EventType eventType = null;
        try {
            eventType = EventType.valueOf(eventDTO.getType());
        } catch (RuntimeException e) {
            eventType = EventType.OTHER;
        }
        return JsonFormat.printer().print(eventService.organizerUpdateEvent(
                EventProto.newBuilder()
                        .setOrganizerId(user.getId())
                        .setId(eventId)
                        .setEventType(eventType.name())
                        .setName(eventDTO.getName())
                        .setInfo(eventDTO.getInfo())
                        .setPrice(Int32Value.newBuilder().setValue(eventDTO.getPrice()).build())
                        .setStartTime(eventDTO.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond())
                        .setLocation(eventDTO.getLocation())
                        .build()));
    }


    private String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
