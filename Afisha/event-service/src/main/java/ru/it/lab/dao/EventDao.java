package ru.it.lab.dao;

import ru.it.lab.SearchProto;
import ru.it.lab.entities.Event;

import java.util.List;

public interface EventDao extends AbstractDao<Event> {
    List<Event> getSearchedEvents(SearchProto searchProto);

    List<Event> getCreatedEventsByUserId(long id);
}
