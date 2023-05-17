package ru.it.lab.service;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import ru.it.lab.CommentsList;
import ru.it.lab.Empty;

import ru.it.lab.EventParticipationsList;
import ru.it.lab.EventProto;
import ru.it.lab.EventServiceGrpc;
import ru.it.lab.EventsList;
import ru.it.lab.Id;
import ru.it.lab.Info;
import ru.it.lab.VotesList;
import ru.it.lab.config.MQEventConfig;
import ru.it.lab.configuration.MQRoleConfig;
import ru.it.lab.dao.EventCommentDao;
import ru.it.lab.dao.EventDao;
import ru.it.lab.dao.EventParticipationDao;
import ru.it.lab.dao.EventVoteDao;
import ru.it.lab.dto.EventRequestDTO;
import ru.it.lab.dto.RoleRequestDTO;
import ru.it.lab.entities.Event;
import ru.it.lab.enums.EventType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@GrpcService
public class EventServerService extends EventServiceGrpc.EventServiceImplBase {
    @Autowired
    private EventCommentDao commentDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private EventParticipationDao eventParticipationDao;

    @Autowired
    private EventVoteDao voteDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Override
    public void getEventByIdWithOrganizerCheck(EventProto request, StreamObserver<EventProto> responseObserver) {
        Event event = eventDao.getById(request.getId());
        if (event.getOrganizerId()!=request.getOrganizerId() && !event.getIsAccepted()) {
            responseObserver.onError(new StatusRuntimeException(Status.CANCELLED.withDescription("You have no permission to access this event")));
        } else {
            responseObserver.onNext(EventProto.newBuilder()
                            .setId(event.getId())
                            .setOrganizerId(event.getOrganizerId())
                            .setInfo(event.getInfo())
                            .setPrice(Int32Value.newBuilder().setValue(event.getPrice()).build())
                            .setStartTime(event.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond())
                            .setLocation(event.getLocation())
                            .setRating(event.getRating())
                            .setIsAccepted(event.getIsAccepted())
                    .build());
            responseObserver.onCompleted();
        }
    }


    @Override
    public void getNotApprovedEventById(Id request, StreamObserver<EventProto> responseObserver) {
        Event event = eventDao.getById(request.getId());
            responseObserver.onNext(EventProto.newBuilder()
                    .setId(event.getId())
                    .setOrganizerId(event.getOrganizerId())
                    .setInfo(event.getInfo())
                    .setPrice(Int32Value.newBuilder().setValue(event.getPrice()).build())
                    .setStartTime(event.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond())
                    .setLocation(event.getLocation())
                    .setRating(event.getRating())
                    .setIsAccepted(event.getIsAccepted())
                    .build());
            responseObserver.onCompleted();
    }

    @Override
    public void getEventsByType(EventProto request, StreamObserver<EventsList> responseObserver) {
        List<Event> events = eventDao.getByEventType(request.getEventType());
        EventsList.Builder eventsList = EventsList.newBuilder();
        for (Event event: events) {
            eventsList.addEvents(EventProto.newBuilder()
                    .setId(event.getId())
                    .setOrganizerId(event.getOrganizerId())
                    .setInfo(event.getInfo())
                    .setPrice(Int32Value.newBuilder().setValue(event.getPrice()))
                    .setStartTime(event.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond())
                    .setLocation(event.getLocation())
                    .setRating(event.getRating())
                    .setIsAccepted(event.getIsAccepted())
                    .build());
        }
        responseObserver.onNext(eventsList.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllEvents(Empty request, StreamObserver<EventsList> responseObserver) {
        List<Event> events = eventDao.getAll();
        EventsList.Builder eventsList = EventsList.newBuilder();
        for (Event event: events) {
            eventsList.addEvents(EventProto.newBuilder()
                    .setId(event.getId())
                    .setOrganizerId(event.getOrganizerId())
                    .setInfo(event.getInfo())
                    .setPrice(Int32Value.newBuilder().setValue(event.getPrice()))
                    .setStartTime(event.getStartTime().atZone(ZoneId.systemDefault()).toEpochSecond())
                    .setLocation(event.getLocation())
                    .setRating(event.getRating())
                    .setIsAccepted(event.getIsAccepted())
                    .build());
        }
        responseObserver.onNext(eventsList.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createEvent(EventProto request, StreamObserver<Info> responseObserver) {
        Event event = new Event();
        event.setOrganizerId(request.getOrganizerId());
        event.setName(request.getName());
        event.setType(EventType.valueOf(request.getEventType()));
        event.setInfo(request.getInfo());
        event.setPrice(request.getPrice().getValue());
        event.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getStartTime()), TimeZone.getDefault().toZoneId()));
        event.setLocation(request.getLocation());
        event = eventDao.create(event);
        rabbitTemplate.convertAndSend(MQEventConfig.EXCHANGE, MQEventConfig.KEY, new EventRequestDTO(event.getId(), LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond() ,event.getOrganizerId()));
        responseObserver.onNext(Info.newBuilder().setInfo("Event created! Wait for admin to approve it!").build());
        responseObserver.onCompleted();
    }

    @Override
    public void acceptEvent(Id request, StreamObserver<Info> responseObserver) {
        Event event = eventDao.getById(request.getId());
        event.setIsAccepted(true);
        eventDao.update(event);
        responseObserver.onNext(Info.newBuilder().setInfo("Event " + event.getName() + " is accepted").build());
        responseObserver.onCompleted();
    }

    @Override
    public void organizerUpdateEvent(EventProto request, StreamObserver<Info> responseObserver) {
        Event event = eventDao.getById(request.getId());
        if (event.getOrganizerId()!=request.getOrganizerId()) {
            responseObserver.onError(new StatusRuntimeException(Status.CANCELLED.withDescription("You are not organizer of this event")));
            return;
        }
        if (!request.getEventType().equals("")) {
            event.setType(EventType.valueOf(request.getEventType()));
        }
        if (!request.getName().equals("")) {
            event.setName(request.getName());
        }
        if(!request.getInfo().equals("")) {
            event.setInfo(request.getInfo());
        }
        if (request.hasPrice()) {
            event.setPrice(request.getPrice().getValue());
        }
        if (request.getStartTime()!=0) {
            event.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getStartTime()), TimeZone.getDefault().toZoneId()));
        }
        if (!request.getLocation().equals("")) {
            event.setLocation(request.getLocation());
        }
        event.setIsAccepted(false);
        eventDao.update(event);
        rabbitTemplate.convertAndSend(MQEventConfig.EXCHANGE, MQEventConfig.KEY, new EventRequestDTO(event.getId(), LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond(), event.getOrganizerId()));
        responseObserver.onNext(Info.newBuilder().setInfo("Event updated! Wait for admin to approve the changes").build());
        responseObserver.onCompleted();
    }

//    @Override
//    public void getVotes(Id request, StreamObserver<VotesList> responseObserver) {
//        super.getVotes(request, responseObserver);
//    }
//
//    @Override
//    public void getFavorites(Id request, StreamObserver<EventParticipationsList> responseObserver) {
//        super.getFavorites(request, responseObserver);
//    }
//
//    @Override
//    public void getComments(Id request, StreamObserver<CommentsList> responseObserver) {
//        super.getComments(request, responseObserver);
//    }
}
