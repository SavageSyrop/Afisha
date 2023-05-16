package ru.it.lab.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.it.lab.ChatServiceGrpc;


@RestController
@RequestMapping("/chat")
public class ChatController {

    @GrpcClient("grpc-chat-service")
    private ChatServiceGrpc.ChatServiceBlockingStub chatService;

//    @GetMapping("/all")
//    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
//    public String getChats() {
//
//    }
//
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
//    public String getChat(@PathVariable Long id) {
//
//    }
//
//    @PostMapping("/{id}")
//    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
//    public String sendMessage(@PathVariable Long id) {
//
//    }

    private String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
