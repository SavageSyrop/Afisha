package ru.it.lab.dao;


import ru.it.lab.entities.EventComment;

import java.util.List;

public interface EventCommentDao extends AbstractDao<EventComment>{
    List<EventComment> getCommentsByEventId(long id);

    List<EventComment> getCommentsByUserId(long id);

    EventComment getCommentByUserAndEventId(long eventId, long userId);
}
