package ru.it.lab.service;

import com.google.protobuf.Int32Value;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import ru.it.lab.Empty;
import ru.it.lab.EventParticipationsList;
import ru.it.lab.EventProto;
import ru.it.lab.EventServiceGrpc;
import ru.it.lab.EventsList;
import ru.it.lab.Id;
import ru.it.lab.Info;
import ru.it.lab.SearchProto;
import ru.it.lab.VoteProto;
import ru.it.lab.VotesList;
import ru.it.lab.config.MQConfig;
import ru.it.lab.dao.EventCommentDao;
import ru.it.lab.dao.EventDao;
import ru.it.lab.dao.EventParticipationDao;
import ru.it.lab.dao.EventVoteDao;
import ru.it.lab.dto.EventRequestDTO;
import ru.it.lab.entities.Event;
import ru.it.lab.entities.EventParticipation;
import ru.it.lab.entities.EventVote;
import ru.it.lab.enums.EventParticipationType;
import ru.it.lab.enums.EventType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

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
    public void getEventById(Id request, StreamObserver<EventProto> responseObserver) {
        Event event = eventDao.getById(request.getId());
        if (!event.getIsAccepted()) {
            responseObserver.onError(new StatusRuntimeException(Status.CANCELLED.withDescription("This event is not yet published")));
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
    public void getMyCreatedEvents(Id request, StreamObserver<EventsList> responseObserver) {
        List<Event> events = eventDao.getCreatedEventsByUserId(request.getId());
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

    // only admin use
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
    public void getAllApprovedEvents(Empty request, StreamObserver<EventsList> responseObserver) {
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
        LocalDateTime localDateTime = Instant.ofEpochSecond((long) request.getStartTime()).atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime();
        event.setStartTime(localDateTime);
        event.setLocation(request.getLocation());
        event.setIsAccepted(false);
        event.setRating(0f);
        event = eventDao.create(event);
        EventRequestDTO eventRequestDTO = new EventRequestDTO();
        eventRequestDTO.setEventId(event.getId());
        eventRequestDTO.setCreation_time(request.getStartTime());
        eventRequestDTO.setOrganizerId(event.getOrganizerId());
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_EVENT, MQConfig.KEY_EVENT, eventRequestDTO);
        responseObserver.onNext(Info.newBuilder().setInfo("Event created! Wait for admin to approve it!").build());
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
            LocalDateTime localDateTime = Instant.ofEpochSecond((long) request.getStartTime()).atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime();
            event.setStartTime(localDateTime);
        }
        if (!request.getLocation().equals("")) {
            event.setLocation(request.getLocation());
        }
        event.setIsAccepted(false);
        eventDao.update(event);
        EventRequestDTO eventRequestDTO = new EventRequestDTO();
        eventRequestDTO.setEventId(event.getId());
        eventRequestDTO.setCreation_time(request.getStartTime());
        eventRequestDTO.setOrganizerId(event.getOrganizerId());
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_EVENT, MQConfig.KEY_EVENT,eventRequestDTO);
        responseObserver.onNext(Info.newBuilder().setInfo("Event updated! Wait for admin to approve the changes").build());
        responseObserver.onCompleted();
    }

    @Override
    public void acceptEventById(Id request, StreamObserver<Info> responseObserver) {
        Event event = eventDao.getById(request.getId());
        event.setIsAccepted(true);
        eventDao.update(event);
        responseObserver.onNext(Info.newBuilder().setInfo("Event " + event.getName() + " is accepted").build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteEventById(Id request, StreamObserver<Info> responseObserver) {
        eventDao.deleteById(request.getId());
        responseObserver.onNext(Info.newBuilder().setInfo("Event is successfully deleted").build());
        responseObserver.onCompleted();
    }

    @Override
    public void addFavorites(ru.it.lab.EventParticipation request, StreamObserver<Info> responseObserver) {
        EventParticipation eventParticipation = eventParticipationDao.getParticipationByUserAndEventId(request.getEventId(),request.getUserId());
        if (eventParticipation!=null) {
            responseObserver.onError(new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription("You have already added this as favorites")));
            return;
        }
        Event event = eventDao.getById(request.getEventId());
        EventParticipation participation = new EventParticipation();
        participation.setEvent(event);
        participation.setParticipationType(EventParticipationType.FAVORITE);
        participation.setUserId(request.getUserId());
        eventParticipationDao.create(participation);
        responseObserver.onNext(Info.newBuilder().setInfo("Event added to favorites").build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteFromFavorites(ru.it.lab.EventParticipation request, StreamObserver<Info> responseObserver) {
        EventParticipation eventParticipation = eventParticipationDao.getParticipationByUserAndEventId(request.getEventId(),request.getUserId());
        if (eventParticipation==null) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("You have not added this as favorites")));
            return;
        }
        eventParticipationDao.deleteById(eventParticipation.getId());
        responseObserver.onNext(Info.newBuilder().setInfo("Event removed from favorites").build());
        responseObserver.onCompleted();
    }


    @Override
    public void getFavoritesByUserId(Id request, StreamObserver<EventParticipationsList> responseObserver) {
        EventParticipationsList.Builder list = EventParticipationsList.newBuilder();
        List<EventParticipation> favorites = eventParticipationDao.getFavoritesByUserId(request.getId());
        for (EventParticipation eventParticipation: favorites) {
            list.addParticipations(ru.it.lab.EventParticipation.newBuilder()
                    .setUserId(eventParticipation.getUserId())
                    .setEventId(eventParticipation.getEvent().getId())
                    .setParticipationType(eventParticipation.getParticipationType().name())
                    .setId(eventParticipation.getId())
                    .build());
        }
        responseObserver.onNext(list.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getApprovedEventsWithPeriodAndType(SearchProto request, StreamObserver<EventsList> responseObserver) {
        EventsList.Builder list = EventsList.newBuilder();
        List<Event> events = eventDao.getSearchedEvents(request);
        for (Event event: events) {
            list.addEvents(EventProto.newBuilder()
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
        responseObserver.onNext(list.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getVotesByUserId(Id request, StreamObserver<VotesList> responseObserver) {
        VotesList.Builder list = VotesList.newBuilder();
        List<EventVote> favorites = voteDao.getVotesByUserId(request.getId());
        for (EventVote eventVote: favorites) {
            list.addVotes(ru.it.lab.VoteProto.newBuilder()
                    .setUserId(eventVote.getUserId())
                    .setEventId(eventVote.getEvent().getId())
                    .setValue(eventVote.getVoteValue())
                    .build());
        }
        responseObserver.onNext(list.build());
        responseObserver.onCompleted();
    }

    @Override
    public void voteEvent(VoteProto request, StreamObserver<Info> responseObserver) {
        EventVote vote = voteDao.getVoteByEventAndUserId(request.getEventId(),request.getUserId());
        if (vote!=null) {
            responseObserver.onError(new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription("You have already voted this event")));
            return;
        }
        vote = new EventVote();
        vote.setVoteValue((short) request.getValue());
        Event event = eventDao.getById(request.getEventId());
        vote.setEvent(event);
        vote.setUserId(request.getUserId());
        voteDao.create(vote);
        Float newRating = voteDao.getAverageVoteByEventId(event.getId()).floatValue();
        event.setRating(newRating);
        eventDao.update(event);
        responseObserver.onNext(Info.newBuilder().setInfo("Vote has been set").build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteVoteFromEvent(VoteProto request, StreamObserver<Info> responseObserver) {
        EventVote vote = voteDao.getVoteByEventAndUserId(request.getEventId(),request.getUserId());
        if (vote==null) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("You have not voted this event")));
            return;
        }
        Event event = eventDao.getById(vote.getEvent().getId());
        voteDao.deleteById(vote.getId());
        Float newRating = voteDao.getAverageVoteByEventId(event.getId()).floatValue();
        event.setRating(newRating);
        eventDao.update(event);
        responseObserver.onNext(Info.newBuilder().setInfo("Vote has been deleted").build());
        responseObserver.onCompleted();
    }

//    @Override
//    public void getCommentsByUserId(Id request, StreamObserver<CommentsList> responseObserver) {
//
//    }
}
