package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.EventParticipationDao;
import ru.it.lab.entities.Event;
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
}
