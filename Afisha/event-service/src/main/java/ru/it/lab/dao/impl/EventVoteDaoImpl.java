package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.EventVoteDao;
import ru.it.lab.entities.EventParticipation;
import ru.it.lab.entities.EventVote;
import ru.it.lab.enums.EventParticipationType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class EventVoteDaoImpl extends AbstractDaoImpl<EventVote> implements EventVoteDao {
    @Override
    public Double getAverageVoteByEventId(Long eventId) {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNativeQuery("SELECT AVG(CAST(vote_value as float)) from event_votes where event_id=:param1");
        query.setParameter("param1",eventId);
        Double res = (Double) query.getSingleResult();
        if (res==null) {
            return 0d;
        } else {
            return res;
        }

    }

    @Override
    public List<EventVote> getVotesByUserId(long id) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventVote> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<EventVote> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<EventVote> crit = criteriaQuery.select(rootEntry)
                .where(
                        criteriaBuilder.equal(rootEntry.get("userId"),id)
                );
        TypedQuery<EventVote> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public EventVote getVoteByEventAndUserId(long eventId, long userId) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventVote> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<EventVote> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<EventVote> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(criteriaBuilder.equal(rootEntry.get("event").get("id"), eventId), criteriaBuilder.equal(rootEntry.get("userId"), userId))
                );
        TypedQuery<EventVote> found = entityManager.createQuery(crit);
        if (found.getResultList().size() == 0) {
            return null;
        }
        return found.getSingleResult();
    }
}
