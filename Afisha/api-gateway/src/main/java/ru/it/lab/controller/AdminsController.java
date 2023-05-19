package ru.it.lab.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.it.lab.AdminServiceGrpc;
import ru.it.lab.Empty;
import ru.it.lab.Id;
import ru.it.lab.RoleRequest;
import ru.it.lab.SupportRequest;
import ru.it.lab.UserProto;
import ru.it.lab.dto.SupportRequestDTO;


@RestController
@RequestMapping("/admin")
public class AdminsController {

    @GrpcClient("grpc-admin-service")
    private AdminServiceGrpc.AdminServiceBlockingStub adminService;


    @GetMapping("/role_requests/all")
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

    @PostMapping("/user/{userId}/ban")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String banUser(@PathVariable Long userId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.banUser(UserProto.newBuilder().setId(userId).build()));
    }

    @PostMapping("/user/{userId}/unban")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String unbanUser(@PathVariable Long userId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.unbanUser(UserProto.newBuilder().setId(userId).build()));
    }

    @GetMapping("/support_requests/open")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String getAllOpenSupportRequests() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.getAllOpenSupportRequests(Empty.newBuilder().build()));
    }

    @PostMapping("/support_request/{requestId}/close")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String closeSupportRequest(@RequestBody SupportRequestDTO supportRequestDTO, @PathVariable Long requestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.closeSupportRequest(SupportRequest.newBuilder()
                .setAnswer(supportRequestDTO.getAnswer())
                .setId(requestId)
                .setUsername(getCurrentUserName())
                .build()));
    }

    @GetMapping("/event_requests/all")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String getAllWaitingForApprovalEvents() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.getAllWaitingForApprovalEvents(Empty.newBuilder().build()));
    }


    @PostMapping("/event_requests/{eventRequestId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String acceptEventRequest(@PathVariable Long eventRequestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.acceptEventRequest(Id.newBuilder().setId(eventRequestId).build()));
    }

    @DeleteMapping("/event_requests/{eventRequestId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String declineEventRequest(@PathVariable Long eventRequestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.declineEventRequest(Id.newBuilder().setId(eventRequestId).build()));
    }

    @DeleteMapping("/events/{eventId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String deleteEvent(@PathVariable Long eventId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.deleteEvent(Id.newBuilder().setId(eventId).build()));
    }

    @DeleteMapping("/events/comments/{commentId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String deleteComment(@PathVariable Long commentId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.deleteComment(Id.newBuilder().setId(commentId).build()));
    }

    private String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
