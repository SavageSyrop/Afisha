package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.ChatDao;
import ru.it.lab.entities.Chat;

@Component
public class ChatDaoImpl extends AbstractDaoImpl<Chat> implements ChatDao {
}
