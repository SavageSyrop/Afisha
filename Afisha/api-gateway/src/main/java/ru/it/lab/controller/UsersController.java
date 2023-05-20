package ru.it.lab.controller;


import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.AccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.it.lab.AuthenticateAndGet;
import ru.it.lab.ChangeUserRequest;
import ru.it.lab.ChatServiceGrpc;
import ru.it.lab.EventServiceGrpc;
import ru.it.lab.Id;
import ru.it.lab.Info;
import ru.it.lab.MessageProto;
import ru.it.lab.ResetPasswordRequest;
import ru.it.lab.SupportRequest;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.configuration.SecurityConstants;
import ru.it.lab.dto.LoginDTO;
import ru.it.lab.dto.SupportRequestDTO;
import ru.it.lab.dto.UserDTO;
import ru.it.lab.entities.Authorization;
import ru.it.lab.entities.Permission;
import ru.it.lab.entities.Role;
import ru.it.lab.enums.PermissionType;
import ru.it.lab.enums.RoleType;
import ru.it.lab.service.AuthorizationService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@Api(value = "user-controller", description = "contains user related endpoints")
public class UsersController {

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @GrpcClient("grpc-events-service")
    private EventServiceGrpc.EventServiceBlockingStub eventService;

    @GrpcClient("grpc-chats-service")
    private ChatServiceGrpc.ChatServiceBlockingStub chatService;

    @Autowired
    private AuthorizationService authorizationService;

    @ApiOperation("Creates authorization cookie for user")
    @PostMapping("login")
    public String login(@RequestBody LoginDTO loginDto, HttpServletRequest req, HttpServletResponse httpResponse) throws IOException {
        UserProto proto = userService.getLoginData(UserProto.newBuilder().setUsername(loginDto.getUsername()).build());
        Authorization authorization = new Authorization();
        authorization.setId(proto.getId());
        authorization.setPassword(proto.getPassword());
        authorization.setUsername(proto.getUsername());
        Role role = new Role();
        List<Permission> permissionList = new ArrayList<>();
        for (ru.it.lab.Permission permission : proto.getRole().getPermissionList()) {
            permissionList.add(new Permission(PermissionType.valueOf(permission.getName())));
        }
        role.setPermissions(permissionList);
        authorization.setRole(role);
        authorization.setIsBanned(proto.getIsBanned());
        authorization.setActivationCode(proto.getActivationCode());
        String token = authorizationService.login(authorization, loginDto.getUsername(), loginDto.getPassword());
        Cookie cookie = new Cookie(SecurityConstants.AUTHORIZATION_COOKIE, token);
        cookie.setMaxAge((int) SecurityConstants.EXPIRATION_TIME);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        httpResponse.addCookie(cookie);
        httpResponse.sendRedirect("/user/my_profile");
        return "";
    }

    @ApiOperation("Removes authorization cookie from user")
    @GetMapping("perform_logout")
    public String logout() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(Info.newBuilder().setInfo("You have been logged out").build());
    }

    @ApiOperation("Registration for new users, sends account authorization email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User entered correct data"),
            @ApiResponse(responseCode = "400", description = "User failed username or password validations")
    })
    @PostMapping("sign_up")
    public String signUp(@RequestBody UserDTO userDTO, HttpServletResponse httpResponse) throws IOException {
        validatePassword(userDTO.getPassword());
        UserProto userReq = UserProto.newBuilder()
                .setUsername(userDTO.getUsername())
                .setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword()))
                .setDateOfBirth(userDTO.getDateOfBirth().getTime())
                .setGenderType(userDTO.getGenderType())
                .setEmail(userDTO.getEmail())
                .setRoleId(userDTO.getRoleId())
                .setIsOpenProfile(userDTO.getIsOpenProfile()).build();
        return (JsonFormat.printer().print(userService.registerUser(userReq)));

    }

    @ApiOperation("Gets current user profile")
    @GetMapping("user/my_profile")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getMyProfile() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build()));
    }

    @ApiOperation("Updates current user profile data, sends activation email if address or username were changed")
    @PostMapping("user/my_profile")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public void changeMyProfileInfo(@RequestBody UserDTO userDTO, HttpServletResponse httpServletResponse) throws IOException {
        ChangeUserRequest.Builder user = ChangeUserRequest.newBuilder();
        user.setOldUsername(getCurrentUserName());
        if (userDTO.getUsername() != null) {
            user.setNewUsername(userDTO.getUsername());
        }

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getDateOfBirth() != null) {
            user.setDateOfBirth(userDTO.getDateOfBirth().getTime());
        }

        if (userDTO.getGenderType() != null) {
            user.setGenderType(userDTO.getGenderType());
        }
        userService.changeUserData(user.build());
        if (userDTO.getUsername() != null || user.getEmail() != null) {
            httpServletResponse.sendRedirect("/perform_logout");
        }
    }

    @ApiOperation("Updates current user password and sends activation email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User entered valid password"),
            @ApiResponse(responseCode = "400", description = "User failed new password validation")
    })
    @PostMapping("user/my_profile/password")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public void changeMyPassword(@RequestParam String newPassword, HttpServletResponse httpServletResponse) throws IOException {
        UserProto.Builder user = UserProto.newBuilder();
        validatePassword(newPassword);
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        user.setUsername(getCurrentUserName());
        httpServletResponse.sendRedirect("/perform_logout");
    }

    @ApiOperation("Gets current user privacy status")
    @GetMapping("user/my_profile/privacy")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getProfilePrivacyStatus() throws InvalidProtocolBufferException {
        UserProto.Builder user = UserProto.newBuilder();
        user.setUsername(getCurrentUserName());
        return JsonFormat.printer().print(userService.getPrivacy(user.build()));
    }

    @ApiOperation("Changes current user privacy status to opposite")
    @PostMapping("user/my_profile/privacy")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String toggleProfilePrivacyStatus() throws InvalidProtocolBufferException {
        UserProto.Builder user = UserProto.newBuilder();
        user.setUsername(getCurrentUserName());
        return JsonFormat.printer().print(userService.togglePrivacy(user.build()));
    }

    @ApiOperation("Gets user profile by id if it is open")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Selected user has public profile"),
            @ApiResponse(responseCode = "401", description = "Selected user has private profile")
    })
    @GetMapping("user/{userId}/profile")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getUserInfo(@PathVariable Long userId) throws AccessException, InvalidProtocolBufferException {
        UserProto user = userService.getUserById(Id.newBuilder().setId(userId).build());
        if (!user.getIsOpenProfile() && !user.getUsername().equals(getCurrentUserName())) {
            throw new AccessException("This user has private profile");
        }
        return JsonFormat.printer().print(user);
    }

    @ApiOperation("Sets password reset code and sends email to user")
    @GetMapping("forgot_password")
    public String forgotPassword(@RequestParam String username) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.forgotPassword(Info.newBuilder().setInfo(username).build()));
    }

    @ApiOperation("Sets new password and removes reset code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User entered valid password"),
            @ApiResponse(responseCode = "400", description = "User failed new password validation")
    })
    @GetMapping("reset_password/{code}")
    public String resetPassword(@PathVariable String code, @RequestParam String newPassword) throws InvalidProtocolBufferException {
        validatePassword(newPassword);
        return JsonFormat.printer().print(userService.resetPassword(ResetPasswordRequest.newBuilder().setCode(code).setNewPassword(new BCryptPasswordEncoder().encode(newPassword)).build()));
    }

    @ApiOperation("Activates account and enables ability to login")
    @GetMapping("activate/{code}")
    public String activateAccount(@PathVariable String code) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.activateAccount(Info.newBuilder().setInfo(code).build()));
    }

    @ApiOperation("Creates new role request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requested role is different from what user already has"),
            @ApiResponse(responseCode = "400", description = "Requested role is same with what user already has")
    })
    @PostMapping("user/request_role")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String requestRole(@RequestParam String roleType) throws InvalidProtocolBufferException {
        RoleType role = RoleType.valueOf(roleType);
        UserProto user = getCurrentUser();
        if (user.getRole().getName().equals(role.name())) {
            throw new IllegalArgumentException("You already have this role!");
        }
        return JsonFormat.printer().print(userService.requestRole(UserProto.newBuilder().setUsername(getCurrentUserName()).setRoleId(role.id).build()));
    }


    @ApiOperation("Gets all support requests from current user")
    @GetMapping("user/my_support_requests/all")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getSupportRequests() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.getSupportRequestsByUsername(AuthenticateAndGet.newBuilder().setUsername(getCurrentUserName()).build()));
    }

    @ApiOperation("Gets support request from current user by id")
    @GetMapping("user/my_support_requests/{idRequest}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getSupportRequestsById(@PathVariable Long idRequest) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.getSupportRequestById(AuthenticateAndGet.newBuilder().setUsername(getCurrentUserName()).setSearchedId(idRequest).build()));
    }

    @ApiOperation("Adds new support request")
    @PostMapping("user/support_requests")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String createSupportRequest(@RequestBody SupportRequestDTO supportRequestDTO) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.createSupportRequest(SupportRequest.newBuilder()
                .setQuestion(supportRequestDTO.getQuestion())
                .setUsername(getCurrentUserName())
                .build()));
    }


    @ApiOperation("Gets favorites from user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Selected user has public account, action success"),
            @ApiResponse(responseCode = "401", description = "Selected user has private account")
    })
    @GetMapping("user/{userId}/favorites")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getFavorites(@PathVariable Long userId) throws InvalidProtocolBufferException, AccessException {
        UserProto userProto = userService.getUserById(Id.newBuilder().setId(userId).build());
        if (userProto.getIsOpenProfile()) {
            return JsonFormat.printer().print(eventService.getFavoritesByUserId(Id.newBuilder().setId(userProto.getId()).build()));
        } else {
            throw new AccessException("This user has private profile");
        }
    }

    @ApiOperation("Gets all added favorites from current user")
    @GetMapping("user/my_favorites")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getMyFavorites() throws InvalidProtocolBufferException {
        UserProto userProto = getCurrentUser();
        return JsonFormat.printer().print(eventService.getFavoritesByUserId(Id.newBuilder().setId(userProto.getId()).build()));
    }


    @ApiOperation("Gets all votes from user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Selected user has public profile, action success"),
            @ApiResponse(responseCode = "401", description = "Selected user has private profile")
    })
    @GetMapping("user/{userId}/votes")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getVotes(@PathVariable Long userId) throws InvalidProtocolBufferException, AccessException {
        UserProto user = userService.getUserById(Id.newBuilder().setId(userId).build());
        if (!user.getIsOpenProfile()) {
            throw new AccessException("This user has private profile");
        }
        return JsonFormat.printer().print(eventService.getVotesByUserId(Id.newBuilder().setId(user.getId()).build()));
    }

    @ApiOperation("Gets all votes from current user")
    @GetMapping("user/my_votes")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String myVotes() throws InvalidProtocolBufferException {
        UserProto user = userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());
        return JsonFormat.printer().print(eventService.getVotesByUserId(Id.newBuilder().setId(user.getId()).build()));
    }

    @ApiOperation("Gets all written comments from user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Selected user has public profile, action success"),
            @ApiResponse(responseCode = "401", description = "Selected user has private profile")
    })
    @GetMapping("user/{userId}/comments")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getComments(@PathVariable Long userId) throws InvalidProtocolBufferException, AccessException {
        UserProto user = userService.getUserById(Id.newBuilder().setId(userId).build());
        if (!user.getIsOpenProfile()) {
            throw new AccessException("This user has private profile");
        }
        return JsonFormat.printer().print(eventService.getCommentsByUserId(Id.newBuilder().setId(user.getId()).build()));
    }

    @ApiOperation("Gets all written comments from current user")
    @GetMapping("user/my_comments")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getMyComments() throws InvalidProtocolBufferException {
        UserProto userProto = userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());
        return JsonFormat.printer().print(eventService.getCommentsByUserId(Id.newBuilder().setId(userProto.getId()).build()));
    }

    @ApiOperation("Writes user by id, creates chat if it is first interaction with selected user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Selected user has public profile, action success"),
            @ApiResponse(responseCode = "400", description = "Current user tries to write himself"),
            @ApiResponse(responseCode = "401", description = "Selected user has private profile")
    })
    @PostMapping("user/{userId}/write_message")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String writeUser(@PathVariable Long userId, @RequestParam String message) throws InvalidProtocolBufferException, AccessException {
        UserProto recipientUser = userService.getUserById(Id.newBuilder().setId(userId).build());
        if (recipientUser.getUsername().equals(getCurrentUserName())) {
            throw new IllegalArgumentException("You can not write yourself!");
        }
        UserProto userProto = getCurrentUser();
        if (!recipientUser.getIsOpenProfile()) {
            throw new AccessException("This user has private profile");
        }
        return JsonFormat.printer().print(chatService.writeUser(MessageProto.newBuilder()
                .setRecipientId(Int64Value.newBuilder().setValue(recipientUser.getId()).build())
                .setSenderId(userProto.getId())
                .setText(message)
                .build()));

    }


    private String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UserProto getCurrentUser() {
        return userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());
    }

    private void validatePassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,20}$"; // 1 большая буква, 1 цифра, длина от 8 до 20 символов
        if (!password.matches(passwordPattern)) {
            throw new IllegalArgumentException("Invalid password");
        }
    }
}
