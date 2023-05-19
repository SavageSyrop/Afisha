package ru.it.lab.dao;

import ru.it.lab.entities.Message;

import java.util.List;

public interface MessageDao extends AbstractDao<Message> {
    List<Message> getMessagesByChatId(long chatId);
}
