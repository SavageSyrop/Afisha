package ru.it.lab.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "admin-controller", description = "contains admin endpoints")
public class AdminsController {

    @GrpcClient("grpc-admin-service")
    private AdminServiceGrpc.AdminServiceBlockingStub adminService;


    @ApiOperation("Gets all unhandled role requests")
    @GetMapping("/role_requests/all")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String getAllRoleRequests() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.getAllRoleRequests(Empty.newBuilder().build()));
    }

    @ApiOperation("Deletes role request by id")
    @DeleteMapping("/role_requests/{roleRequestId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String declineRequest(@PathVariable Long roleRequestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.declineRoleRequest(RoleRequest.newBuilder()
                .setId(roleRequestId)
                .build()));
    }

    @ApiOperation("Accepts role request by id, deletes request, updates user")
    @PostMapping("/role_requests/{roleRequestId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String acceptRequest(@PathVariable Long roleRequestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.acceptRoleRequest(RoleRequest.newBuilder()
                .setId(roleRequestId)
                .build()));
    }

    @ApiOperation("Blocks user by id from accessing account, updates user")
    @PostMapping("/user/{userId}/ban")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String banUser(@PathVariable Long userId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.banUser(UserProto.newBuilder().setId(userId).build()));
    }

    @ApiOperation("Unblocks user by id from accessing account, updates user")
    @PostMapping("/user/{userId}/unban")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String unbanUser(@PathVariable Long userId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.unbanUser(UserProto.newBuilder().setId(userId).build()));
    }

    @ApiOperation("Gets all unhandled support requests")
    @GetMapping("/support_requests/open")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String getAllOpenSupportRequests() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.getAllOpenSupportRequests(Empty.newBuilder().build()));
    }

    @ApiOperation("Answers support request by id and deletes it, sends email about request being answered to user")
    @PostMapping("/support_requests/{requestId}/close")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String closeSupportRequest(@RequestBody SupportRequestDTO supportRequestDTO, @PathVariable Long requestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.closeSupportRequest(SupportRequest.newBuilder()
                .setAnswer(supportRequestDTO.getAnswer())
                .setId(requestId)
                .setUsername(getCurrentUserName())
                .build()));
    }

    @ApiOperation("Gets all event approval requests")
    @GetMapping("/event_requests/all")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String getAllWaitingForApprovalEvents() throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.getAllWaitingForApprovalEvents(Empty.newBuilder().build()));
    }


    @ApiOperation("Approves event by id, deletes requests, updates event")
    @PostMapping("/event_requests/{eventRequestId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String acceptEventRequest(@PathVariable Long eventRequestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.acceptEventRequest(Id.newBuilder().setId(eventRequestId).build()));
    }

    @ApiOperation("Deletes event request by id, deletes event")
    @DeleteMapping("/event_requests/{eventRequestId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String declineEventRequest(@PathVariable Long eventRequestId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.declineEventRequest(Id.newBuilder().setId(eventRequestId).build()));
    }

    @ApiOperation("Deletes event by id")
    @DeleteMapping("/events/{eventId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String deleteEvent(@PathVariable Long eventId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.deleteEvent(Id.newBuilder().setId(eventId).build()));
    }

    @ApiOperation("Deletes comment by id")
    @DeleteMapping("/events/comments/{commentId}")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public String deleteComment(@PathVariable Long commentId) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(adminService.deleteComment(Id.newBuilder().setId(commentId).build()));
    }

    private String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
