package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.EventCommentDao;
import ru.it.lab.entities.EventComment;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class EventCommentDaoImpl extends AbstractDaoImpl<EventComment> implements EventCommentDao {
    @Override
    public List<EventComment> getCommentsByEventId(long id) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventComment> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<EventComment> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<EventComment> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(rootEntry.get("event").get("id"), id));
        TypedQuery<EventComment> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public List<EventComment> getCommentsByUserId(long id) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventComment> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<EventComment> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<EventComment> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(rootEntry.get("userId"), id));
        TypedQuery<EventComment> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public EventComment getCommentByUserAndEventId(long eventId, long userId) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventComment> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<EventComment> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<EventComment> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(criteriaBuilder.equal(
                        rootEntry.get("event").get("id"),
                        eventId), criteriaBuilder.equal(rootEntry.get("userId"), userId))

                );
        TypedQuery<EventComment> foundUsers = entityManager.createQuery(crit);
        if (foundUsers.getResultList().size() == 0) {
            return null;
        }
        return foundUsers.getSingleResult();
    }
}
