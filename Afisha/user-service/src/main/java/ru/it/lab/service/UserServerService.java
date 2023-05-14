package ru.it.lab.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.it.lab.AuthenticateAndGet;
import ru.it.lab.ChangeUserRequest;
import ru.it.lab.Empty;
import ru.it.lab.Info;
import ru.it.lab.ResetPasswordRequest;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.dao.RoleDao;
import ru.it.lab.dao.SupportRequestDao;
import ru.it.lab.dao.UserDao;
import ru.it.lab.entities.Permission;
import ru.it.lab.entities.Role;
import ru.it.lab.entities.SupportRequest;
import ru.it.lab.entities.User;
import ru.it.lab.enums.GenderType;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@GrpcService
public class UserServerService extends UserServiceGrpc.UserServiceImplBase {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private SupportRequestDao supportRequestDao;

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
        if (userDetails.getIsBanned() == null) {

        }
        if (userDetails.getActivationCode() == null) {
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
            validateEmail(request.getEmail());
            validateUsername(request.getUsername());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage())));
            return;
        }
        user.setActivationCode(UUID.randomUUID().toString());
        user.setIsBanned(false);
        try {
            user = userDao.create(user);
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription("User with username " + user.getUsername() + " already exists")));
        }
        mailService.sendActivationEmail(user);
        responseObserver.onNext(Info.newBuilder().setInfo("To complete registration follow instructions in mail sent at " + user.getEmail()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void changeUserData(ChangeUserRequest request, StreamObserver<Empty> responseObserver) {
        User user = userDao.getByUsername(request.getOldUsername());
        boolean emailChanged = false;
        if (!request.getNewUsername().isEmpty()) {
            try {
                validateUsername(request.getNewUsername());
            } catch (IllegalArgumentException e) {
                responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage())));

            }
            user.setUsername(request.getNewUsername());
        }
        if (!request.getEmail().isEmpty()) {
            try {
                validateEmail(request.getEmail());
            } catch (IllegalArgumentException e) {
                responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage())));

            }
            user.setEmail(request.getEmail());
            user.setActivationCode(UUID.randomUUID().toString());
            emailChanged = true;
        }
        if (request.getDateOfBirth() != 0) {
            user.setDateOfBirth(new Date(request.getDateOfBirth()));
        }
        if (!request.getGenderType().equals("")) {
            user.setGenderType(GenderType.valueOf(request.getGenderType()));
        }
        try {
            userDao.update(user);
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription("User with username " + user.getUsername() + " already exists")));
        }
        if (emailChanged) {
            mailService.sendActivationEmail(user);
        }
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
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
            info += " open";
        } else {
            info += "private";
        }
        responseObserver.onNext(Info.newBuilder().setInfo(info).build());
        responseObserver.onCompleted();
    }

    @Override
    public void togglePrivacy(UserProto request, StreamObserver<Info> responseObserver) {
        User user = userDao.getByUsername(request.getUsername());
        String info = "Your profile is now ";
        if (!user.getIsOpenProfile()) {
            info += " open";
            user.setIsOpenProfile(true);
        } else {
            info += "private";
            user.setIsOpenProfile(false);
        }
        userDao.update(user);
        responseObserver.onNext(Info.newBuilder().setInfo(info).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserByUsername(UserProto request, StreamObserver<UserProto> responseObserver) {
        User user = userDao.getByUsername(request.getUsername());
        if (user == null) {
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


    @Override
    public void activateAccount(Info request, StreamObserver<Info> responseObserver) {
        User user = userDao.getByActivationCode(request.getInfo());
        if (user == null) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("Invalid activation code")));
        }
        user.setActivationCode(null);
        userDao.update(user);
        responseObserver.onNext(Info.newBuilder().setInfo("Your account has been activated! Go ahead and login!").build());
        responseObserver.onCompleted();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, StreamObserver<Info> responseObserver) {
        User user = userDao.getByResetCode(request.getCode());
        if (user == null) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("Invalid reset code")));
        }
        user.setPassword(request.getNewPassword());
        user.setRestorePasswordCode(null);
        userDao.update(user);
        responseObserver.onNext(Info.newBuilder().setInfo("New password is set! Go ahead and login!").build());
        responseObserver.onCompleted();
    }

    @Override
    public void forgotPassword(Info request, StreamObserver<Info> responseObserver) {
        User user = userDao.getByUsername(request.getInfo());
        if (user == null) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("User " + request.getInfo() + " not found")));
        }
        user.setRestorePasswordCode(UUID.randomUUID().toString());
        userDao.update(user);
        mailService.sendForgotPasswordEmail(user);
        responseObserver.onNext(Info.newBuilder().setInfo("To reset password follow instructions in email sent to you " + user.getEmail()).build());
        responseObserver.onCompleted();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void requestRole(UserProto request, StreamObserver<Info> responseObserver) {

    }

    @Override
    public void requestSupport(SupportRequest request, StreamObserver<Info> responseObserver) {

    }

    @Override
    public void getSupportRequests(AuthenticateAndGet request, StreamObserver<ru.it.lab.SupportRequest> responseObserver) {
        List<SupportRequest> supportRequestList = supportRequestDao.getAll();
        for (SupportRequest req: supportRequestList) {
            ru.it.lab.SupportRequest.Builder response = ru.it.lab.SupportRequest.newBuilder();
            response.setId(req.getId());
            response.setUserId(req.getUser().getId());
            if (req.getAdmin()!=null) {
                response.setAdminId(req.getAdmin().getId());
            }
            response.set(req.getQuestion());
            response.set
            responseObserver.onNext(response.build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getSupportRequest(AuthenticateAndGet request, StreamObserver<SupportRequest> responseObserver) {

    }


    private boolean validateEmail(String email) {
        String emailPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        if (!email.matches(emailPattern)) {
            throw new IllegalArgumentException("Invalid email");
        }

        return true;
    }

    private boolean validateUsername(String username) {
        String usernamePattern = "^[a-zA-Z0-9_.]{5,20}$";
        if (!username.matches(usernamePattern)) {
            throw new IllegalArgumentException("Invalid username");
        }
        return true;
    }
}
