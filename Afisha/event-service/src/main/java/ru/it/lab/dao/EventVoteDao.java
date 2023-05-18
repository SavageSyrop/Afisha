package ru.it.lab.dao;

import ru.it.lab.entities.Event;
import ru.it.lab.entities.EventVote;

import java.util.List;

public interface EventVoteDao extends AbstractDao<EventVote>{
    Double getAverageVoteByEventId(Long eventId);

    List<EventVote> getVotesByUserId(long id);

    EventVote getVoteByEventAndUserId(long eventId, long userId);
}
