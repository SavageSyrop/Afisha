package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.EventDao;
import ru.it.lab.entities.Event;
import ru.it.lab.enums.EventType;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class EventDaoImpl extends AbstractDaoImpl<Event> implements EventDao {
    @Override
    public List<Event> getApprovedEventsByEventType(String eventType) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Event> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Event> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(criteriaBuilder.equal(rootEntry.get("type"), EventType.valueOf(eventType)), criteriaBuilder.isTrue(rootEntry.get("isAccepted")))
                );
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
