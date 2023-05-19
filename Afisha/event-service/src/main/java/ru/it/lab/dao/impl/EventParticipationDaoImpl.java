package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.EventParticipationDao;
import ru.it.lab.entities.EventParticipation;
import ru.it.lab.enums.EventParticipationType;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class EventParticipationDaoImpl extends AbstractDaoImpl<EventParticipation> implements EventParticipationDao {
    @Override
    public List<EventParticipation> getFavoritesByUserId(long id) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventParticipation> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<EventParticipation> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<EventParticipation> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(criteriaBuilder.equal(rootEntry.get("userId"), id), criteriaBuilder.notEqual(rootEntry.get("participationType"), EventParticipationType.ORGANIZER))
                );
        TypedQuery<EventParticipation> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public EventParticipation getParticipationByUserAndEventId(long eventId, long userId) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventParticipation> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<EventParticipation> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<EventParticipation> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(criteriaBuilder.equal(rootEntry.get("event").get("id"), eventId), criteriaBuilder.equal(rootEntry.get("userId"), userId), criteriaBuilder.equal(rootEntry.get("participationType"), EventParticipationType.FAVORITE)

                ));
        TypedQuery<EventParticipation> found = entityManager.createQuery(crit);
        if (found.getResultList().size() == 0) {
            return null;
        }
        return found.getSingleResult();
    }
}
