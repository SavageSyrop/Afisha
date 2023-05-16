package ru.it.lab.service;


import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.it.lab.AdminServiceGrpc;
import ru.it.lab.Empty;
import ru.it.lab.Info;
import ru.it.lab.UserProto;
import ru.it.lab.entities.RoleRequest;
import ru.it.lab.RoleRequestList;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.dao.RoleRequestDao;

import javax.persistence.EntityNotFoundException;
import java.time.ZoneId;


@GrpcService
public class AdminServerService extends AdminServiceGrpc.AdminServiceImplBase {

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @Autowired
    private RoleRequestDao requestDao;

    @Override
    public void getAllRoleRequests(Empty request, StreamObserver<RoleRequestList> responseObserver) {
        RoleRequestList.Builder reqs = RoleRequestList.newBuilder();
        for (RoleRequest roleRequest: requestDao.getAll()) {
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
            RoleRequest roleRequest = requestDao.getById(request.getId());
            Info info = userService.setRole(UserProto.newBuilder().setUsername(roleRequest.getUsername()).setRoleId(roleRequest.getRoleId()).build());
            requestDao.deleteById(request.getId());
            responseObserver.onNext(info);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException e) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
        }
    }

    @Override
    public void declineRoleRequest(ru.it.lab.RoleRequest request, StreamObserver<Info> responseObserver) {
        try {
            requestDao.deleteById(request.getId());
        } catch (EntityNotFoundException e) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage())));
        }
        responseObserver.onNext(Info.newBuilder().setInfo("Role request denied!").build());
        responseObserver.onCompleted();
    }



}
