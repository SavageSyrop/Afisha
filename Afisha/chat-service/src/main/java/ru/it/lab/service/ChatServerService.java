package ru.it.lab.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.it.lab.ChatParticipationProto;
import ru.it.lab.ChatParticipationsList;
import ru.it.lab.ChatProto;
import ru.it.lab.ChatServiceGrpc;
import ru.it.lab.Id;
import ru.it.lab.Info;
import ru.it.lab.MessageProto;
import ru.it.lab.MessagesList;
import ru.it.lab.UserProto;
import ru.it.lab.UserServiceGrpc;
import ru.it.lab.dao.ChatDao;
import ru.it.lab.dao.ChatParticipationDao;
import ru.it.lab.dao.MessageDao;
import ru.it.lab.entities.Chat;
import ru.it.lab.entities.ChatParticipation;
import ru.it.lab.entities.Message;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@GrpcService
public class ChatServerService extends ChatServiceGrpc.ChatServiceImplBase {

    @GrpcClient("grpc-users-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @Autowired
    private ChatDao chatDao;
    @Autowired
    private ChatParticipationDao chatParticipationDao;
    @Autowired
    private MessageDao messageDao;


    @Override
    public void writeUser(MessageProto request, StreamObserver<Info> responseObserver) {
        if (request.hasChatId()) {  // когда поступил chatId
            Chat chat = chatDao.getById(request.getChatId().getValue());    // проверка существования чата
            ChatParticipation senderParticipation = chatParticipationDao.getUserParticipationInChatByChatId(request.getChatId().getValue(), request.getSenderId());
            if (senderParticipation == null) {        // когда попытка доступа не в свой чат
                responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription("You are not participant in this chat")));
                return;
            } else {
                Message message = new Message();
                message.setText(request.getText());
                message.setChat(chat);
                message.setSenderId(request.getSenderId());
                message.setSendingTime(LocalDateTime.now());
                messageDao.create(message);
                responseObserver.onNext(Info.newBuilder().setInfo("Message sent!").build());
                responseObserver.onCompleted();
            }
        } else {        // когда поступил recipientId
            Chat chat = chatParticipationDao.getChatBetweenCurrentAndRecipientUsers(request.getSenderId(), request.getRecipientId().getValue());
            UserProto recipient = userService.getUserById(Id.newBuilder().setId(request.getRecipientId().getValue()).build());
            UserProto sender = userService.getUserById(Id.newBuilder().setId(request.getSenderId()).build());
            if (chat == null) {   // еще не общались
                chat = new Chat();
                chat.setName(sender.getUsername() + " x " + recipient.getUsername());
                chat = chatDao.create(chat);
                ChatParticipation senderParticipation = new ChatParticipation();
                senderParticipation.setChat(chat);
                senderParticipation.setUserId(sender.getId());
                chatParticipationDao.create(senderParticipation);
                ChatParticipation recipientParticipation = new ChatParticipation();
                recipientParticipation.setChat(chat);
                recipientParticipation.setUserId(recipient.getId());
                chatParticipationDao.create(recipientParticipation);
            }
            Message message = new Message();
            message.setChat(chat);
            message.setText(request.getText());
            message.setSenderId(sender.getId());
            message.setSendingTime(LocalDateTime.now());
            messageDao.create(message);
            responseObserver.onNext(Info.newBuilder().setInfo("Message sent!").build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getChat(ChatParticipationProto request, StreamObserver<ChatProto> responseObserver) {
        ChatParticipation chatParticipation = chatParticipationDao.getUserParticipationInChatByChatId(request.getChatId(), request.getUserId());
        if (chatParticipation == null) {
            responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription("You are not participant in this chat")));
            return;
        }
        Chat chat = chatDao.getById(chatParticipation.getChat().getId());
        responseObserver.onNext(ChatProto.newBuilder().setId(chat.getId()).setName(chat.getName()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getChats(ChatParticipationProto request, StreamObserver<ChatParticipationsList> responseObserver) {
        List<ChatParticipation> list = chatParticipationDao.getChatParticipationsByUserId(request.getUserId());
        ChatParticipationsList.Builder chatList = ChatParticipationsList.newBuilder();
        for (ChatParticipation participation : list) {
            chatList.addChats(ChatParticipationProto.newBuilder()
                    .setId(participation.getId())
                    .setUserId(participation.getUserId())
                    .setChatName(participation.getChat().getName())
                    .build());
        }
        responseObserver.onNext(chatList.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getMessagesFromChat(ChatParticipationProto request, StreamObserver<MessagesList> responseObserver) {
        ChatParticipation chatParticipation = chatParticipationDao.getUserParticipationInChatByChatId(request.getChatId(), request.getUserId());
        if (chatParticipation == null) {
            responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription("You are not participant in this chat")));
            return;
        }
        List<Message> messages = messageDao.getMessagesByChatId(request.getChatId());
        MessagesList.Builder messagesList = MessagesList.newBuilder();
        for (Message message : messages) {
            messagesList.addMessages(MessageProto.newBuilder()
                            .setId(message.getId())
                            .setSenderId(message.getSenderId())
                            .setText(message.getText())
                            .setSendingTime(message.getSendingTime().atZone(ZoneId.systemDefault()).toEpochSecond()))
                    .build();
        }
        responseObserver.onNext(messagesList.build());
        responseObserver.onCompleted();
    }

    @Override
    public void renameChat(ChatParticipationProto request, StreamObserver<Info> responseObserver) {
        ChatParticipation chatParticipation = chatParticipationDao.getUserParticipationInChatByChatId(request.getChatId(), request.getUserId());
        if (chatParticipation == null) {
            responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription("You are not participant in this chat")));
            return;
        }
        Chat chat = chatDao.getById(chatParticipation.getChat().getId());
        chat.setName(request.getChatName());
        chatDao.update(chat);
        responseObserver.onNext(Info.newBuilder().setInfo("Chat renamed").build());
        responseObserver.onCompleted();
    }
}
