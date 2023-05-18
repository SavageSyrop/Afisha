package ru.it.lab.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import ru.it.lab.AuthenticateAndGet;
import ru.it.lab.ChangeUserRequest;
import ru.it.lab.Empty;
import ru.it.lab.Info;
import ru.it.lab.ResetPasswordRequest;

import ru.it.lab.SupportRequestsStream;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.config.MQConfig;
import ru.it.lab.dao.RoleDao;
import ru.it.lab.dao.SupportRequestDao;
import ru.it.lab.dao.UserDao;
import ru.it.lab.dto.RoleRequestDTO;
import ru.it.lab.entities.Permission;
import ru.it.lab.entities.Role;
import ru.it.lab.entities.SupportRequest;
import ru.it.lab.entities.User;
import ru.it.lab.enums.GenderType;
import ru.it.lab.enums.RoleType;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void getLoginData(UserProto request, StreamObserver<UserProto> responseObserver) {
        User userDetails = userDao.getByUsername(request.getUsername());
        if (userDetails == null) {
            responseObserver.onError(new EntityNotFoundException("User with username " + request.getUsername() + " doesn't exist"));
            throw new EntityNotFoundException("User with username " + request.getUsername() + " doesn't exist");
        }
        ru.it.lab.Role.Builder role = ru.it.lab.Role.newBuilder();
        role.setName(userDetails.getRole().getName().name());

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
            user.clearIsBanned();
        } else {
            user.setIsBanned(userDetails.getIsBanned());
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


    @Override
    public void requestRole(UserProto request, StreamObserver<Info> responseObserver) {
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_ROLE, MQConfig.KEY_ROLE, new RoleRequestDTO(request.getUsername(), LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond(), request.getRoleId()));
        responseObserver.onNext(Info.newBuilder().setInfo("Request added!").build());
        responseObserver.onCompleted();
    }

    @Override
    public void setRole(UserProto request, StreamObserver<Info> responseObserver) {
        User user = userDao.getByUsername(request.getUsername());
        Role role = roleDao.getById(request.getRoleId());
        user.setRole(role);
        userDao.update(user);
        responseObserver.onNext(Info.newBuilder().setInfo(user.getUsername() + " is now " + role.getName().toString()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void banUser(UserProto request, StreamObserver<Info> responseObserver) {
        User user = userDao.getById(request.getId());
        if (user.getRole().getName() == RoleType.ADMIN) {
            responseObserver.onError(new StatusRuntimeException(Status.CANCELLED.withDescription("Admin can not be banned")));
            return;
        }
        if (user.getIsBanned()) {
            responseObserver.onError(new StatusRuntimeException(Status.CANCELLED.withDescription("User is already banned")));
            return;
        }
        user.setIsBanned(true);
        userDao.update(user);
        responseObserver.onNext(Info.newBuilder().setInfo("User " + user.getUsername() + " is banned!").build());
        responseObserver.onCompleted();
    }

    @Override
    public void unbanUser(UserProto request, StreamObserver<Info> responseObserver) {
        User user = userDao.getById(request.getId());
        if (!user.getIsBanned()) {
            responseObserver.onError(new StatusRuntimeException(Status.CANCELLED.withDescription("User is not banned")));
        }
        user.setIsBanned(false);
        userDao.update(user);
        responseObserver.onNext(Info.newBuilder().setInfo("User " + user.getUsername() + " is unbanned!").build());
        responseObserver.onCompleted();
    }


    @Override
    public void getSupportRequestsByUsername(AuthenticateAndGet request, StreamObserver<SupportRequestsStream> responseObserver) {
        List<SupportRequest> supportRequestList = supportRequestDao.getAllByUser(request.getUsername());
        SupportRequestsStream.Builder stream = SupportRequestsStream.newBuilder();
        for (SupportRequest req : supportRequestList) {
            ru.it.lab.SupportRequest.Builder reqBuilder = ru.it.lab.SupportRequest.newBuilder();
            reqBuilder.setId(req.getId());
            reqBuilder.setUserId(req.getUser().getId());
            if (req.getAdmin() != null) {
                reqBuilder.setAdminId(req.getAdmin().getId());
            }
            reqBuilder.setQuestion(req.getQuestion());
            if (req.getAnswer() != null) {
                reqBuilder.setAnswer(req.getAnswer());
            }
            reqBuilder.setCreationTime(req.getCreationTime().atZone(ZoneId.systemDefault()).toEpochSecond());
            if (req.getCloseTime() != null) {
                reqBuilder.setAnsweredTime(req.getCloseTime().atZone(ZoneId.systemDefault()).toEpochSecond());
            }
            stream.addRequests(reqBuilder.build());

        }
        responseObserver.onNext(stream.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getSupportRequestById(AuthenticateAndGet request, StreamObserver<ru.it.lab.SupportRequest> responseObserver) {
        ru.it.lab.SupportRequest.Builder response = ru.it.lab.SupportRequest.newBuilder();
        try {
            SupportRequest supportRequest = supportRequestDao.getById(request.getSearchedId());
            if (!Objects.equals(supportRequest.getUser().getUsername(), request.getUsername())) {
                responseObserver.onError(new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription("You can not access that support request")));
            }
            response.setId(supportRequest.getId());
            response.setUserId(supportRequest.getUser().getId());
            if (supportRequest.getAdmin() != null) {
                response.setAdminId(supportRequest.getAdmin().getId());
            }
            response.setQuestion(supportRequest.getQuestion());
            if (supportRequest.getAnswer() != null) {
                response.setAnswer(supportRequest.getAnswer());
            }
            response.setCreationTime(supportRequest.getCreationTime().atZone(ZoneId.systemDefault()).toEpochSecond());
            if (supportRequest.getCloseTime() != null) {
                response.setAnsweredTime(supportRequest.getCloseTime().atZone(ZoneId.systemDefault()).toEpochSecond());
            }
        } catch (EntityNotFoundException e) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("Support request no found")));
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createSupportRequest(ru.it.lab.SupportRequest request, StreamObserver<Info> responseObserver) {
        SupportRequest supportRequest = new SupportRequest();
        User user = userDao.getByUsername(request.getUsername());
        if (user == null) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("User not found")));
            return;
        }
        supportRequest.setUser(user);
        supportRequest.setQuestion(request.getQuestion());
        supportRequest.setCreationTime(LocalDateTime.now());
        supportRequestDao.create(supportRequest);
        responseObserver.onNext(Info.newBuilder().setInfo("Support request created").build());
        responseObserver.onCompleted();
    }

    @Override
    public void closeSupportRequest(ru.it.lab.SupportRequest request, StreamObserver<Info> responseObserver) {
        SupportRequest supportRequest = supportRequestDao.getById(request.getId());
        if (supportRequest.getCloseTime() != null) {
            responseObserver.onError(new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription("Support request already closed")));
            return;
        }
        User admin = userDao.getByUsername(request.getUsername());
        supportRequest.setAdmin(admin);
        supportRequest.setAnswer(request.getAnswer());
        supportRequest.setCloseTime(LocalDateTime.now());
        supportRequestDao.update(supportRequest);
        responseObserver.onNext(Info.newBuilder().setInfo("Support request closed").build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllOpenSupportRequests(Empty request, StreamObserver<SupportRequestsStream> responseObserver) {
        List<SupportRequest> supportRequestList = supportRequestDao.getAllOpenRequests();
        SupportRequestsStream.Builder stream = SupportRequestsStream.newBuilder();
        for (SupportRequest req : supportRequestList) {
            ru.it.lab.SupportRequest.Builder reqBuilder = ru.it.lab.SupportRequest.newBuilder();
            reqBuilder.setId(req.getId());
            reqBuilder.setUserId(req.getUser().getId());
            reqBuilder.setQuestion(req.getQuestion());
            reqBuilder.setCreationTime(req.getCreationTime().atZone(ZoneId.systemDefault()).toEpochSecond());
            stream.addRequests(reqBuilder.build());
        }
        responseObserver.onNext(stream.build());
        responseObserver.onCompleted();
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
