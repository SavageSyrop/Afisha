package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.MessageDao;
import ru.it.lab.entities.Message;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class MessageDaoImpl extends AbstractDaoImpl<Message> implements MessageDao {


    @Override
    public List<Message> getMessagesByChatId(long chatId) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Message> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Message> crit = criteriaQuery.select(rootEntry)
                .where(
                        criteriaBuilder.equal(rootEntry.get("chat").get("id"), chatId)
                );
        TypedQuery<Message> found = entityManager.createQuery(crit);
        return found.getResultList();
    }
}
