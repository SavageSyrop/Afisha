package ru.it.lab.dao;

import ru.it.lab.entities.EventParticipation;

import java.util.List;

public interface EventParticipationDao extends AbstractDao<EventParticipation> {
    List<EventParticipation> getFavoritesByUserId(long id);

    EventParticipation getParticipationByUserAndEventId(long eventId, long userId);
}
