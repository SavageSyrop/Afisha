package ru.it.lab.dao;

import ru.it.lab.entities.Event;

import java.util.List;

public interface EventDao extends AbstractDao<Event> {
    List<Event> getByEventType(String eventType);
}
