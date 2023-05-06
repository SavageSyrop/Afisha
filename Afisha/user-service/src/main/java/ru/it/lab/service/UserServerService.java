package ru.it.lab.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.it.lab.Empty;
import ru.it.lab.Info;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.dao.RoleDao;
import ru.it.lab.dao.UserDao;
import ru.it.lab.entitities.Permission;
import ru.it.lab.entitities.Role;
import ru.it.lab.entitities.User;
import ru.it.lab.enums.GenderType;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.UUID;


@GrpcService
public class UserServerService extends UserServiceGrpc.UserServiceImplBase {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private MailService mailService;

    @Override
    public void login(UserProto request, StreamObserver<UserProto> responseObserver) {
        User userDetails = userDao.getByUsername(request.getUsername());
        if (userDetails == null) {
            responseObserver.onError(new EntityNotFoundException("User with username " + request.getUsername() + " doesn't exist"));
            throw new EntityNotFoundException("User with username " + request.getUsername() + " doesn't exist");
        }
        ru.it.lab.Role.Builder role = ru.it.lab.Role.newBuilder();

        for (Permission per : userDetails.getRole().getPermissions()) {
            ru.it.lab.Permission permission = ru.it.lab.Permission.newBuilder().setName(per.getName().name()).build();
            role.addPermission(permission);
        }

        UserProto.Builder user = UserProto.newBuilder()
                .setUsername(userDetails.getUsername())
                .setPassword(userDetails.getPassword())
                .setRole(role.build())
                .setIsBanned(userDetails.getIsBanned());
        if (userDetails.getActivationCode()==null) {
            user.clearActivationCode();
        } else {
            user.setActivationCode(userDetails.getActivationCode());
        }


        responseObserver.onNext(user.build());
        responseObserver.onCompleted();
    }

    @Override
    public void registerUser(UserProto request, StreamObserver<Info> responseObserver) {
        Role role = roleDao.getById(request.getRoleId());
        User user = new User(request.getUsername(), request.getPassword(), request.getEmail(), new Date(request.getDateOfBirth()), GenderType.valueOf(request.getGenderType()), role, request.getIsOpenProfile());
        try {
            validateEmail(request);
            validateUsername(request);
        } catch (IllegalStateException e) {
            responseObserver.onError(e);
            return;
        }
        user.setActivationCode(UUID.randomUUID().toString());
        user = userDao.create(user);
        mailService.sendActivationEmail(user);
        responseObserver.onNext(Info.newBuilder().setInfo("To complete registration follow instructions in mail sent at " + user.getEmail()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void changeUserData(UserProto request, StreamObserver<Empty> responseObserver) {
        User user = userDao.getByUsername(request.getUsername());
        if (!request.getUsername().isEmpty()) {
            validateUsername(request);
            user.setUsername(request.getUsername());
        }
        if (!request.getEmail().isEmpty()) {
            validateEmail(request);
            user.setEmail(request.getEmail());

            user.setActivationCode(UUID.randomUUID().toString());
        }
        if (request.getDateOfBirth()!=0) {
            user.setDateOfBirth(new Date(request.getDateOfBirth()));
        }
        if (!request.getGenderType().equals("")) {
            user.setGenderType(GenderType.valueOf(request.getGenderType()));
        }
        userDao.update(user);
    }


    @Override
    public void changePassword(UserProto request, StreamObserver<Empty> responseObserver) {
        User user = userDao.getByUsername(request.getUsername());
        user.setPassword(request.getPassword());
        userDao.update(user);
        responseObserver.onCompleted();
    }



    @Override
    public void getPrivacy(UserProto request, StreamObserver<Info> responseObserver) {
        User user = userDao.getByUsername(request.getUsername());
        String info = "Your profile is ";
        if (user.getIsOpenProfile()) {
            info+=" open";
        } else {
            info+="private";
        }
        responseObserver.onNext(Info.newBuilder().setInfo(info).build());
    }

    @Override
    public void togglePrivacy(UserProto request, StreamObserver<Info> responseObserver) {
        User user = userDao.getByUsername(request.getUsername());
        String info = "Your profile is now ";
        if (!user.getIsOpenProfile()) {
            info+=" open";
            user.setIsOpenProfile(true);
        } else {
            info+="private";
            user.setIsOpenProfile(false);
        }
        userDao.update(user);
        responseObserver.onNext(Info.newBuilder().setInfo(info).build());
    }

    @Override
    public void getUserByUsername(UserProto request, StreamObserver<UserProto> responseObserver) {
        User user = userDao.getByUsername(request.getUsername());
        if (user==null) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("User with username " + request.getUsername() + " not found")));
            return;
        }
        responseObserver.onNext(UserProto.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setGenderType(user.getGenderType().name())
                .setDateOfBirth(user.getDateOfBirth().getTime())
                .setIsOpenProfile(user.getIsOpenProfile())
                .setRole(ru.it.lab.Role.newBuilder().setName(user.getRole().getName().name()).build()).build());
        responseObserver.onCompleted();
    }

    private boolean validateEmail(UserProto proto) {
        String emailPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        if (!proto.getEmail().matches(emailPattern)) {
            throw new IllegalArgumentException("Invalid email");
        }

        return true;
    }

    private boolean validateUsername(UserProto proto) {
        String usernamePattern = "^[a-zA-Z0-9_.]{5,20}$";
        if (!proto.getUsername().matches(usernamePattern)) {
            throw new IllegalArgumentException("Invalid username");
        }
        return true;
    }


}
