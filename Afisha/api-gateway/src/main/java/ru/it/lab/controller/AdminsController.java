package ru.it.lab.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.it.lab.AdminServiceGrpc;
import ru.it.lab.Empty;
import ru.it.lab.RoleRequest;


@RestController
@RequestMapping("/admin")
public class AdminsController {


    @GrpcClient("grpc-admin-service")
    private  AdminServiceGrpc.AdminServiceBlockingStub adminService;


    @GetMapping("/role_requests")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String getAllRoleRequests() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.getAllRoleRequests(Empty.newBuilder().build()));
    }

    @DeleteMapping("/role_requests/{roleRequestId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String declineRequest(@PathVariable Long roleRequestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.declineRoleRequest(RoleRequest.newBuilder()
                        .setId(roleRequestId)
                .build()));
    }

    @PostMapping("/role_requests/{roleRequestId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String acceptRequest(@PathVariable Long roleRequestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.acceptRoleRequest(RoleRequest.newBuilder()
                .setId(roleRequestId)
                .build()));
    }

//    @GetMapping("/grantRole/{userId}")
//    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
//    public String grandRole(@PathVariable Long userId) {
//
//    }
//
//
//    @DeleteMapping("/delete/event/{eventId}")
//    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
//    public String deleteEvent(@PathVariable Long eventId) {
//
//    }
//
//    @DeleteMapping("/delete/comment/{id}")
//    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
//    public String deleteComment(@PathVariable Long id) {
//
//    }
//
//    @PostMapping("/user/{userId}/ban")
//    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
//    public String banUser(@PathVariable Long id) {
//
//    }
//
//    @PostMapping("/user/{userId}/unban")
//    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
//    public String unbanUser(@PathVariable Long id) {
//
//    }

}
