package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.EventDao;
import ru.it.lab.entities.Event;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class EventDaoImpl extends AbstractDaoImpl<Event> implements EventDao {
    @Override
    public List<Event> getByEventType(String eventType) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Event> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Event> crit = criteriaQuery.select(rootEntry)
                .where(
                        criteriaBuilder.equal(rootEntry.get("type"), eventType)
                );
        TypedQuery<Event> found = entityManager.createQuery(crit);
        return found.getResultList();
    }
}
