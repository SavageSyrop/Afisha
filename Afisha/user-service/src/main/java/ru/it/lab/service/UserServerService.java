package ru.it.lab.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.it.lab.RegisterResponse;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.dao.RoleDao;
import ru.it.lab.dao.UserDao;
import ru.it.lab.entitities.Permission;
import ru.it.lab.entitities.Role;
import ru.it.lab.entitities.User;

import javax.persistence.EntityNotFoundException;
import java.util.List;


@GrpcService
public class UserServerService extends UserServiceGrpc.UserServiceImplBase {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Override
    public void login(UserProto request, StreamObserver<UserProto> responseObserver) {
        User userDetails = userDao.getByUsername(request.getUsername());
        if (userDetails == null) {
            responseObserver.onError(new EntityNotFoundException("User with username " + request.getUsername() + " doesn't exist"));
            throw new EntityNotFoundException("User with username " + request.getUsername() + " doesn't exist");
        }
        ru.it.lab.Role.Builder role = ru.it.lab.Role.newBuilder();
        int count = 1;
        for (Permission per: userDetails.getRole().getPermissions()) {
            role.setPermission(count, ru.it.lab.Permission.newBuilder().setName(per.getName().name()).build());
            count++;
        }
        responseObserver.onNext(UserProto.newBuilder().setUsername(userDetails.getUsername()).setPassword(userDetails.getPassword()).setRole(role.build()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void registerUser(UserProto request, StreamObserver<RegisterResponse> responseObserver) {
        List<User> user = userDao.getAll();
        if (user.size()!=2) {
            responseObserver.onNext(RegisterResponse.newBuilder()
                    .setStatus(405).build());
        } else {
            Role role = roleDao.getById(request.getRoleId());
            userDao.create(new User(request.getUsername(),request.getPassword(),request.getEmail(),request.getAge(),role,request.getIsOpenProfile()));
            responseObserver.onNext(RegisterResponse.newBuilder()
                    .setStatus(200).build());
        }
        responseObserver.onCompleted();
    }
}
