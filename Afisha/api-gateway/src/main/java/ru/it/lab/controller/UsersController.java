package ru.it.lab.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.dto.LoginDTO;
import ru.it.lab.dto.UserDTO;
import ru.it.lab.entitities.Authorization;
import ru.it.lab.entitities.Permission;
import ru.it.lab.entitities.Role;
import ru.it.lab.enums.PermissionType;
import ru.it.lab.service.AuthorizationService;
import com.google.protobuf.util.JsonFormat;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class UsersController {

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;


    @Autowired
    private AuthorizationService authorizationService;


    @GetMapping("login")
    public void login(@RequestBody LoginDTO loginDto, HttpServletResponse httpResponse) throws IOException {
        UserProto proto = userService.login(UserProto.newBuilder().setUsername(loginDto.getUsername()).build());
        Authorization authorization = new Authorization();
        authorization.setId(proto.getId());
        authorization.setPassword(proto.getPassword());
        authorization.setUsername(proto.getUsername());
        Role role = new Role();
        List<Permission> permissionList = new ArrayList<>();
        for (ru.it.lab.Permission permission: proto.getRole().getPermissionList()) {
            permissionList.add(new Permission(PermissionType.valueOf(permission.getName())));
        }
        role.setPermissions(permissionList);
        authorization.setRole(role);
        authorizationService.login(authorization, loginDto.getUsername(), loginDto.getPassword());
        httpResponse.sendRedirect("/user/myprofile");
    }

    @PostMapping("sign_up")
    public void signUp(@RequestBody UserDTO userDTO, HttpServletResponse httpResponse) {
        UserProto userReq = UserProto.newBuilder()
                .setUsername(userDTO.getUsername())
                .setPassword(userDTO.getPassword())
                .setAge(userDTO.getAge())
                .setEmail(userDTO.getEmail())
                .setRoleId(userDTO.getRole().getId())
                .setIsOpenProfile(userDTO.getIsOpenProfile()).build();
        httpResponse.setStatus(userService.registerUser(userReq).getStatus());
    }

    @GetMapping("user/myprofile")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getMyProfile() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build()));
    }



//    @GetMapping("/{userId}/profile}")
//    public ResponseEntity<UserDTO> getUserInfo(@PathVariable String username) {
//        return ResponseEntity.ok();
//    }

private String getCurrentUserName() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
}
}
