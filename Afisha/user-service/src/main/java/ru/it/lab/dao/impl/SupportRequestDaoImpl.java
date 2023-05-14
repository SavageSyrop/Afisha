package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.SupportRequestDao;
import ru.it.lab.entities.SupportRequest;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class SupportRequestDaoImpl extends AbstractDaoImpl<SupportRequest> implements SupportRequestDao {
    @Override
    public List<SupportRequest> getAllByUserId(Long userId) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SupportRequest> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<SupportRequest> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<SupportRequest> crit = criteriaQuery.select(rootEntry)
                .where(
                        criteriaBuilder.equal(rootEntry.get("user").get("id"), userId)
                );
        TypedQuery<SupportRequest> found = entityManager.createQuery(crit);
        return found.getResultList();
    }
}
