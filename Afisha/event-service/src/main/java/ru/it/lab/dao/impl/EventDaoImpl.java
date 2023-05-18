package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.SearchProto;
import ru.it.lab.dao.EventDao;
import ru.it.lab.entities.Event;
import ru.it.lab.enums.EventType;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Component
public class EventDaoImpl extends AbstractDaoImpl<Event> implements EventDao {
    @Override
    public List<Event> getSearchedEvents(SearchProto searchProto) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Event> rootEntry = criteriaQuery.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.isTrue(rootEntry.get("isAccepted")));

        if (searchProto.hasType()) {
            predicates.add(criteriaBuilder.equal(rootEntry.get("type"), EventType.valueOf(searchProto.getType().getValue())));
        }

        if (searchProto.hasSelectedDate()) {
            LocalDateTime selectedDate = Instant.ofEpochMilli((long) searchProto.getSelectedDate().getValue()).atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime();
            LocalDateTime nextDay = Instant.ofEpochMilli((long) searchProto.getSelectedDate().getValue() + 86400000).atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime();
            predicates.add(criteriaBuilder.between(rootEntry.get("startTime"), selectedDate, nextDay));
        }

        if (searchProto.hasFrom() && searchProto.hasTo()) {
            LocalDateTime to = Instant.ofEpochMilli((long) searchProto.getTo().getValue()).atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime();
            LocalDateTime from = Instant.ofEpochMilli((long) searchProto.getFrom().getValue()).atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime();
            predicates.add(criteriaBuilder.between(rootEntry.get("startTime"), from, to));
        }


        CriteriaQuery<Event> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        TypedQuery<Event> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public List<Event> getCreatedEventsByUserId(long id) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Event> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Event> crit = criteriaQuery.select(rootEntry)
                .where(
                        criteriaBuilder.equal(rootEntry.get("organizerId"), id)
                );
        TypedQuery<Event> found = entityManager.createQuery(crit);
        return found.getResultList();
    }
}
