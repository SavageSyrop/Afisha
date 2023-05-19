package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.EventApprovalRequestDao;
import ru.it.lab.entities.EventApprovalRequest;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Component
public class EventApprovalRequestDaoImpl extends AbstractDaoImpl<EventApprovalRequest> implements EventApprovalRequestDao {

    @Override
    public EventApprovalRequest getByEventId(long id) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventApprovalRequest> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<EventApprovalRequest> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<EventApprovalRequest> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(
                        rootEntry.get("eventId"),
                        id)
                );
        TypedQuery<EventApprovalRequest> foundUsers = entityManager.createQuery(crit);
        if (foundUsers.getResultList().size() == 0) {
            return null;
        }
        return foundUsers.getSingleResult();
    }
}
