package ru.it.lab.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.it.lab.AdminServiceGrpc;


@RestController
@RequestMapping("/admin")
public class AdminsController {


    @GrpcClient("grpc-admin-service")
    private  AdminServiceGrpc.AdminServiceBlockingStub adminService;

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
