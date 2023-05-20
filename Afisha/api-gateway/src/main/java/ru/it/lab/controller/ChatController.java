package ru.it.lab.controller;

import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.swagger.annotations.Api;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.it.lab.ChatParticipationProto;
import ru.it.lab.ChatServiceGrpc;
import ru.it.lab.MessageProto;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;


@RestController
@RequestMapping("/chats")
@Api(value = "chat-controller")
public class ChatController {

    @GrpcClient("grpc-chats-service")
    private ChatServiceGrpc.ChatServiceBlockingStub chatService;

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getChats() throws InvalidProtocolBufferException {
        UserProto userProto = getCurrentUser();
        return JsonFormat.printer().print(chatService.getChats(ChatParticipationProto.newBuilder()
                .setUserId(userProto.getId())
                .build()));
    }


    @GetMapping("/{chatId}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getChat(@PathVariable Long chatId) throws InvalidProtocolBufferException {
        UserProto userProto = getCurrentUser();
        return JsonFormat.printer().print(chatService.getChat(ChatParticipationProto.newBuilder()
                .setChatId(chatId)
                .setUserId(userProto.getId())
                .build()));
    }

    @GetMapping("/{chatId}/messages")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getMessages(@PathVariable Long chatId) throws InvalidProtocolBufferException {
        UserProto userProto = getCurrentUser();
        return JsonFormat.printer().print(chatService.getMessagesFromChat(ChatParticipationProto.newBuilder()
                .setChatId(chatId)
                .setUserId(userProto.getId())
                .build()));
    }


    @PostMapping("/{chatId}/messages")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String sendMessage(@PathVariable Long chatId, @RequestParam String message) throws InvalidProtocolBufferException {
        UserProto userProto = getCurrentUser();
        return JsonFormat.printer().print(chatService.writeUser(MessageProto.newBuilder()
                .setChatId(Int64Value.newBuilder().setValue(chatId).build())
                .setSenderId(userProto.getId())
                .setText(message)
                .build()));
    }


    @PostMapping("/{chatId}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String renameChat(@PathVariable Long chatId, @RequestParam String chatName) throws InvalidProtocolBufferException {
        UserProto user = getCurrentUser();
        return JsonFormat.printer().print(chatService.renameChat(ChatParticipationProto.newBuilder()
                .setChatName(chatName)
                .setChatId(chatId)
                .setUserId(user.getId())
                .build()
        ));
    }


    private String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UserProto getCurrentUser() {
        return userService.getUserByUsername(UserProto.newBuilder().setUsername(getCurrentUserName()).build());
    }
}
