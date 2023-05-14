package ru.it.lab.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.it.lab.ChatServiceGrpc;


@RestController
@RequestMapping("/chat")
public class ChatController {

    @GrpcClient("grpc-chat-service")
    private ChatServiceGrpc.ChatServiceBlockingStub chatService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getChats() {

    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getChat(@PathVariable Long id) {

    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String sendMessage(@PathVariable Long id) {

    }


}
