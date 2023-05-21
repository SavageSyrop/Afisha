package ru.it.lab.service;


import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.it.lab.AdminServiceGrpc;
import ru.it.lab.Empty;
import ru.it.lab.EventApprovalRequestList;
import ru.it.lab.EventApprovalRequestProto;
import ru.it.lab.EventServiceGrpc;
import ru.it.lab.Id;
import ru.it.lab.Info;
import ru.it.lab.RoleRequestList;
import ru.it.lab.SupportRequest;
import ru.it.lab.SupportRequestsStream;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.dao.EventApprovalRequestDao;
import ru.it.lab.dao.RoleRequestDao;
import ru.it.lab.entities.EventApprovalRequest;
import ru.it.lab.entities.RoleRequest;

import javax.persistence.EntityNotFoundException;
import java.time.ZoneId;


@GrpcService
@Slf4j
public class AdminServerService extends AdminServiceGrpc.AdminServiceImplBase {

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @GrpcClient("grpc-events-service")
    private EventServiceGrpc.EventServiceBlockingStub eventService;

    @Autowired
    private RoleRequestDao roleRequestDao;

    @Autowired
    private EventApprovalRequestDao eventRequestDao;

    @Override
    public void getAllRoleRequests(Empty request, StreamObserver<RoleRequestList> responseObserver) {
        RoleRequestList.Builder reqs = RoleRequestList.newBuilder();
        for (RoleRequest roleRequest : roleRequestDao.getAll()) {
            reqs.addRequests(ru.it.lab.RoleRequest.newBuilder()
                    .setRoleId(roleRequest.getRoleId())
                    .setId(roleRequest.getId())
                    .setCreationTime(roleRequest.getCreationTime().atZone(ZoneId.systemDefault()).toEpochSecond())
                    .setUsername(roleRequest.getUsername()));
        }
        responseObserver.onNext(reqs.build());
        responseObserver.onCompleted();
    }

    @Override
    public void acceptRoleRequest(ru.it.lab.RoleRequest request, StreamObserver<Info> responseObserver) {
        try {
            RoleRequest roleRequest = roleRequestDao.getById(request.getId());
            Info info = userService.setRole(UserProto.newBuilder().setUsername(roleRequest.getUsername()).setRoleId(roleRequest.getRoleId()).build());
            roleRequestDao.deleteById(request.getId());
            responseObserver.onNext(info);
            log.info("Admin action: Role is given");
            responseObserver.onCompleted();
        } catch (EntityNotFoundException e) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
            return;
        }
    }

    @Override
    public void declineRoleRequest(ru.it.lab.RoleRequest request, StreamObserver<Info> responseObserver) {
        try {
            roleRequestDao.deleteById(request.getId());
        } catch (EntityNotFoundException e) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
            return;
        }
        responseObserver.onNext(Info.newBuilder().setInfo("Role request denied!").build());
        log.info("Admin action: Role request declined");
        responseObserver.onCompleted();
    }


    @Override
    public void banUser(UserProto request, StreamObserver<Info> responseObserver) {
        responseObserver.onNext(userService.banUser(request));
        log.info("Admin action: User " + request.getUsername() + " is banned");
        responseObserver.onCompleted();
    }

    @Override
    public void unbanUser(UserProto request, StreamObserver<Info> responseObserver) {
        responseObserver.onNext(userService.unbanUser(request));
        log.info("Admin action: User " + request.getUsername() + " is unbanned");
        responseObserver.onCompleted();
    }

    @Override
    public void getAllOpenSupportRequests(Empty request, StreamObserver<SupportRequestsStream> responseObserver) {
        responseObserver.onNext(userService.getAllOpenSupportRequests(Empty.newBuilder().build()));
        responseObserver.onCompleted();
    }

    @Override
    public void closeSupportRequest(SupportRequest request, StreamObserver<Info> responseObserver) {
        responseObserver.onNext(userService.closeSupportRequest(request));
        log.info("Admin action: Support request closed");
        responseObserver.onCompleted();
    }

    @Override
    public void getAllWaitingForApprovalEvents(Empty request, StreamObserver<EventApprovalRequestList> responseObserver) {
        EventApprovalRequestList.Builder list = EventApprovalRequestList.newBuilder();
        for (EventApprovalRequest eventApprovalRequest : eventRequestDao.getAll()) {
            list.addRequests(EventApprovalRequestProto.newBuilder()
                    .setId(eventApprovalRequest.getId())
                    .setCreationTime(eventApprovalRequest.getCreationTime().atZone(ZoneId.systemDefault()).toEpochSecond())
                    .setOrganizerId(eventApprovalRequest.getOrganizerId())
                    .setEventId(eventApprovalRequest.getEventId())
                    .build());
        }
        responseObserver.onNext(list.build());
        responseObserver.onCompleted();
    }

    @Override
    public void acceptEventRequest(Id request, StreamObserver<Info> responseObserver) {
        EventApprovalRequest eventRequest = eventRequestDao.getById(request.getId());
        responseObserver.onNext(eventService.acceptEventById(Id.newBuilder().setId(eventRequest.getEventId()).build()));
        eventRequestDao.deleteById(eventRequest.getId());
        log.info("Admin action: Event request accepted");
        responseObserver.onCompleted();
    }

    @Override
    public void declineEventRequest(Id request, StreamObserver<Info> responseObserver) {
        EventApprovalRequest eventApprovalRequest = eventRequestDao.getById(request.getId());
        responseObserver.onNext(eventService.deleteEventById(Id.newBuilder().setId(eventApprovalRequest.getEventId()).build()));
        eventRequestDao.deleteById(eventApprovalRequest.getId());
        log.info("Admin action: Event request closed");
        responseObserver.onCompleted();
    }

    @Override
    public void deleteEvent(Id request, StreamObserver<Info> responseObserver) {
        EventApprovalRequest eventApprovalRequest = eventRequestDao.getByEventId(request.getId());
        if (eventApprovalRequest != null) {
            eventRequestDao.deleteById(eventApprovalRequest.getId());
        }
        responseObserver.onNext(eventService.deleteEventById(Id.newBuilder().setId(request.getId()).build()));
        log.info("Admin action: Event deleted");
        responseObserver.onCompleted();
    }

    @Override
    public void deleteComment(Id request, StreamObserver<Info> responseObserver) {
        responseObserver.onNext(eventService.deleteComment(request));
        log.info("Admin action: Comment deleted");
        responseObserver.onCompleted();
    }
}
