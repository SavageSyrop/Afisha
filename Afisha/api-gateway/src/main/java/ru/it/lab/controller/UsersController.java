package ru.it.lab.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
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
import ru.it.lab.EventServiceGrpc;
import ru.it.lab.Info;
import ru.it.lab.ResetPasswordRequest;
import ru.it.lab.SupportRequest;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.configuration.SecurityConstants;
import ru.it.lab.dto.SupportRequestDTO;
import ru.it.lab.dto.LoginDTO;
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
public class UsersController {

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @GrpcClient("grpc-events-service")
    private EventServiceGrpc.EventServiceBlockingStub eventService;

    @Autowired
    private AuthorizationService authorizationService;


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
        Cookie cookie = new Cookie(SecurityConstants.AUTHORIZATION_COOKIE,token);
        cookie.setMaxAge((int) SecurityConstants.EXPIRATION_TIME);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        httpResponse.addCookie(cookie);
        httpResponse.sendRedirect("/user/myprofile");
        return "";
    }

    @GetMapping("perform_logout")
    public String logout() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(Info.newBuilder().setInfo("You have been logged out").build());
    }

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

    @GetMapping("user/myprofile")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getMyProfile() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build()));
    }

    @PostMapping("user/myprofile")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public void changeMyProfileInfo(@RequestBody UserDTO userDTO, HttpServletResponse httpServletResponse) throws IOException {
        ChangeUserRequest.Builder user = ChangeUserRequest.newBuilder();
        user.setOldUsername(getCurrentUserName());
        if (userDTO.getUsername()!=null) {
            user.setNewUsername(userDTO.getUsername());
        }

        if (userDTO.getEmail()!=null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getDateOfBirth()!=null){
            user.setDateOfBirth(userDTO.getDateOfBirth().getTime());
        }

        if (userDTO.getGenderType()!=null) {
            user.setGenderType(userDTO.getGenderType());
        }
        userService.changeUserData(user.build());
        if (userDTO.getUsername()!=null || user.getEmail()!=null) {
            httpServletResponse.sendRedirect("/perform_logout");
        }
    }

    @PostMapping("user/myprofile/password")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public void changeMyPassword(@RequestParam String newPassword,  HttpServletResponse httpServletResponse) throws IOException {
        UserProto.Builder user = UserProto.newBuilder();
        validatePassword(newPassword);
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        user.setUsername(getCurrentUserName());
        httpServletResponse.sendRedirect("/perform_logout");
    }

    @GetMapping("user/myprofile/privacy")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getProfilePrivacyStatus() throws InvalidProtocolBufferException {
        UserProto.Builder user = UserProto.newBuilder();
        user.setUsername(getCurrentUserName());
        return JsonFormat.printer().print(userService.getPrivacy(user.build()));
    }

    @PostMapping("user/myprofile/privacy")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String toggleProfilePrivacyStatus() throws InvalidProtocolBufferException {
        UserProto.Builder user = UserProto.newBuilder();
        user.setUsername(getCurrentUserName());
        return JsonFormat.printer().print(userService.togglePrivacy(user.build()));
    }


    @GetMapping("user/{username}/profile")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getUserInfo(@PathVariable String username) throws AccessException, InvalidProtocolBufferException {
        UserProto user = userService.getUserByUsername(UserProto.newBuilder().setUsername(username).build());
        if (!user.getIsOpenProfile() && !user.getUsername().equals(getCurrentUserName())) {
            return JsonFormat.printer().print(Info.newBuilder().setInfo("This user has private profile"));
        }
        return JsonFormat.printer().print(userService.getUserByUsername(UserProto.newBuilder().setUsername(username).build()));
    }



    @GetMapping("forgot_password")
    public String forgotPassword(@RequestParam String username) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.forgotPassword(Info.newBuilder().setInfo(username).build()));
    }

    @GetMapping("reset_password/{code}")
    public String resetPassword(@PathVariable String code,@RequestParam String newPassword) throws InvalidProtocolBufferException {
        validatePassword(newPassword);
        return JsonFormat.printer().print(userService.resetPassword(ResetPasswordRequest.newBuilder().setCode(code).setNewPassword(new BCryptPasswordEncoder().encode(newPassword)).build()));
    }


    @GetMapping("activate/{code}")
    public String activateAccount(@PathVariable String code) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.activateAccount(Info.newBuilder().setInfo(code).build()));
    }

    @PostMapping("user/request_role")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String requestRole(@RequestParam String roleType) throws InvalidProtocolBufferException {
        RoleType role = RoleType.valueOf(roleType);
        UserProto user = userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());
        if (user.getRole().getName().equals(role.toString()) || user.getRole().getName().equals(RoleType.USER.name())) {
            throw new IllegalArgumentException("You already have this role!");
        }
        return JsonFormat.printer().print(userService.requestRole(UserProto.newBuilder().setUsername(getCurrentUserName()).setRoleId(role.id).build()));
    }


    @GetMapping("user/my_support_request/all")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getSupportRequests() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.getSupportRequestsByUsername(AuthenticateAndGet.newBuilder().setUsername(getCurrentUserName()).build()));
    }

    @GetMapping("user/my_support_request/{idRequest}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getSupportRequestsById(@PathVariable Long idRequest) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.getSupportRequestById(AuthenticateAndGet.newBuilder().setUsername(getCurrentUserName()).setSearchedId(idRequest).build()));
    }

    @PostMapping("user/support_request")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String createSupportRequest(@RequestBody SupportRequestDTO supportRequestDTO) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.createSupportRequest(SupportRequest.newBuilder()
                        .setQuestion(supportRequestDTO.getQuestion())
                        .setUsername(getCurrentUserName())
                .build()));
    }

//
//    @GetMapping("user/{userId}/favorites")
//    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
//    public String getFavorites(@PathVariable Long userId) {
//
//    }
//
//    @GetMapping("user/{userId}/votes")
//    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
//    public String getVotes(@PathVariable Long userId) {
//
//    }
//
//    @GetMapping("user/{userId}/comments")
//    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
//    public String getComments(@PathVariable Long userId) {
//
//    }

    private String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void validatePassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,20}$";
        if (!password.matches(passwordPattern)) {
            throw new IllegalArgumentException("Invalid password");
        }
    }
}