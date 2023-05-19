package ru.it.lab.dao;

import ru.it.lab.entities.Chat;
import ru.it.lab.entities.ChatParticipation;

import java.util.List;

public interface ChatParticipationDao extends AbstractDao<ChatParticipation> {
    Chat getChatBetweenCurrentAndRecipientUsers(Long id, Long recId);
    ChatParticipation getUserParticipationInChatByChatId(Long chatId, Long userId);

    List<ChatParticipation> getChatParticipationsByUserId(long userId);
}
