package ru.it.lab.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class UsersController {

    @GrpcClient("grpc-users-service")
    UserServiceGrpc.UserServiceBlockingStub userService;


    @Autowired
    private AuthorizationService authorizationService;


    @GetMapping("login")
    public void login(@RequestBody LoginDTO loginDto, HttpServletResponse httpResponse) throws IOException {
        UserProto proto = userService.login(UserProto.newBuilder().setUsername(loginDto.getUsername()).build());
        Authorization authorization = new Authorization();
        authorization.setPassword(proto.getPassword());
        authorization.setUsername(proto.getUsername());
        Role role = new Role();
        List<Permission> permissionList = new ArrayList<>();
        for (ru.it.lab.Permission permission: proto.getRole().getPermissionList()) {
            permissionList.add(new Permission(PermissionType.valueOf(permission.getName())));
        }
        role.setPermissions(permissionList);
        authorization.setRole(role);
        authorizationService.login(authorization, loginDto.getPassword());
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
    public String getMyProfile() {
        return "sas";
    }


//
//    @GetMapping("/{userId}/profile}")
//    public ResponseEntity<UserDTO> getUserInfo(@PathVariable Long userId) {
//        return ResponseEntity.ok();
//    }
}
