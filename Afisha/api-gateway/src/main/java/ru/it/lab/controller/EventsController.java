package ru.it.lab.controller;

import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.JsonFormat;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.it.lab.CommentProto;
import ru.it.lab.Empty;
import ru.it.lab.EventParticipation;
import ru.it.lab.EventProto;
import ru.it.lab.EventServiceGrpc;
import ru.it.lab.Id;
import ru.it.lab.SearchProto;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.VoteProto;
import ru.it.lab.dto.EventDTO;
import ru.it.lab.dto.SearchDTO;
import ru.it.lab.enums.EventType;

import java.time.ZoneId;

@RestController
@RequestMapping("/events")
public class EventsController {

    @GrpcClient("grpc-events-service")
    private EventServiceGrpc.EventServiceBlockingStub eventService;

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;


    @GetMapping("/{eventId}")
    public String getEventById(@PathVariable Long eventId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(eventService.getEventById(Id.newBuilder().setId(eventId).build()));
    }

    @GetMapping("/my_created_events")
    @PreAuthorize("hasAuthority('CREATING_ACTIONS')")
    public String getMyCreatedEvents() throws InvalidProtocolBufferException {
        UserProto user =  getCurrentUser();
        return JsonFormat.printer().print(eventService.getMyCreatedEvents(Id.newBuilder().setId(user.getId()).build()));
    }

    @GetMapping("/all")
    public String getApprovedEvents() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(eventService.getAllApprovedEvents(Empty.newBuilder().build()));
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
        UserProto user =  getCurrentUser();

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
        UserProto user =  getCurrentUser();
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

    @PostMapping("/{eventId}/favorites")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String addToFavorites(@PathVariable Long eventId) throws InvalidProtocolBufferException {
        UserProto user =  getCurrentUser();
        return JsonFormat.printer().print(eventService.addFavorites(EventParticipation.newBuilder().setEventId(eventId).setUserId(user.getId()).build()));
    }

    @DeleteMapping("/{eventId}/favorites")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String deleteFavorites(@PathVariable Long eventId) throws InvalidProtocolBufferException {
        UserProto user =  getCurrentUser();
        return JsonFormat.printer().print(eventService.deleteFromFavorites(EventParticipation.newBuilder().setEventId(eventId).setUserId(user.getId()).build()));
    }


    @GetMapping("/search")
    public String search(@RequestBody SearchDTO searchDTO) throws InvalidProtocolBufferException {
        SearchProto.Builder searchProto = SearchProto.newBuilder();
        if (searchDTO.getType() != null) {
            EventType eventType = null;
            try {
                eventType = EventType.valueOf(searchDTO.getType());
            } catch (RuntimeException runtimeException) {
                eventType = EventType.OTHER;
            }
            searchProto.setType(StringValue.newBuilder().setValue(eventType.name()));
        }

        if (searchDTO.getTo() != null) {
            searchProto.setTo(Int64Value.newBuilder().setValue(searchDTO.getTo().getTime()).build());
        }

        if (searchDTO.getFrom() != null) {
            searchProto.setFrom(Int64Value.newBuilder().setValue(searchDTO.getFrom().getTime()).build());
        }

        if (searchDTO.getSelectedDate() != null) {
            searchProto.clearFrom();
            searchProto.clearTo();
            searchProto.setSelectedDate(Int64Value.newBuilder().setValue(searchDTO.getSelectedDate().getTime()).build());
        }

        return JsonFormat.printer().print(eventService.getApprovedEventsWithPeriodAndType(searchProto.build()));
    }


    @PostMapping("/{eventId}/vote")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String voteEvent(@PathVariable Long eventId, @RequestParam Integer vote) throws InvalidProtocolBufferException {
        UserProto user =  getCurrentUser();
        return JsonFormat.printer().print(eventService.voteEvent(VoteProto.newBuilder().setEventId(eventId).setValue(vote).setUserId(user.getId()).build()));
    }

    @DeleteMapping("/{eventId}/vote")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String deleteVoteEvent(@PathVariable Long eventId) throws InvalidProtocolBufferException {
        UserProto user =  getCurrentUser();
        return JsonFormat.printer().print(eventService.deleteVoteFromEvent(VoteProto.newBuilder().setEventId(eventId).setUserId(user.getId()).build()));
    }

    @GetMapping("/{eventId}/comments")
    public String getCommentsByEvent(@PathVariable Long eventId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(eventService.getCommentsByEventId(Id.newBuilder().setId(eventId).build()));
    }

    @PostMapping("/{eventId}/comments")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String createComment(@PathVariable Long eventId, @RequestParam String comment) throws InvalidProtocolBufferException {
        UserProto user =  getCurrentUser();
        return JsonFormat.printer().print(eventService.createComment(CommentProto.newBuilder()
                .setUserId(user.getId())
                .setEventId(eventId)
                .setInfo(comment)
                .build()));
    }

    @PostMapping("/{eventId}/comments/{commentId}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String editComment(@PathVariable Long commentId, @RequestParam String newComment) throws InvalidProtocolBufferException {
        UserProto user =  getCurrentUser();
        return JsonFormat.printer().print(eventService.editComment(CommentProto.newBuilder().setId(commentId).setInfo(newComment).setUserId(user.getId()).build()));
    }


    private String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UserProto getCurrentUser() {
        return userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());
    }
}
