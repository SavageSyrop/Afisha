package ru.it.lab.dao.impl;

import org.springframework.stereotype.Component;
import ru.it.lab.dao.ChatParticipationDao;
import ru.it.lab.entities.Chat;
import ru.it.lab.entities.ChatParticipation;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.List;

@Component
public class ChatParticipationDaoImpl extends AbstractDaoImpl<ChatParticipation> implements ChatParticipationDao {


    @Override
    public ChatParticipation getUserParticipationInChatByChatId(Long chatId, Long userId) {
        Class entityClass = ChatParticipation.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChatParticipation> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<ChatParticipation> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<ChatParticipation> critCurrent = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder
                        .and(
                                criteriaBuilder.equal(
                                        rootEntry.get("chat").get("id"),
                                        chatId),
                                criteriaBuilder.equal(
                                        rootEntry.get("userId"),
                                        userId)
                        )
                );
        TypedQuery<ChatParticipation> foundChatParticipation = entityManager.createQuery(critCurrent);
        if (foundChatParticipation.getResultList().size() == 0) {
            return null;
        }
        return foundChatParticipation.getSingleResult();
    }

    @Override
    public List<ChatParticipation> getChatParticipationsByUserId(long userId) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChatParticipation> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<ChatParticipation> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<ChatParticipation> crit = criteriaQuery.select(rootEntry)
                .where(
                        criteriaBuilder.equal(rootEntry.get("userId"), userId)
                );
        TypedQuery<ChatParticipation> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public Chat getChatBetweenCurrentAndRecipientUsers(Long id, Long recId) {
        Class entityClass = ChatParticipation.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChatParticipation> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<ChatParticipation> rootEntry = criteriaQuery.from(entityClass);

        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
        Root<ChatParticipation> subRoot = subquery.from(entityClass);
        subquery.select(subRoot.get("chat").get("id")).where(criteriaBuilder.equal(subRoot.get("userId"), recId));

        criteriaQuery.select(rootEntry).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("userId"), id),
                        criteriaBuilder.in(rootEntry.get("chat").get("id")).value(subquery)
                )
        );

        TypedQuery<ChatParticipation> foundChatParticipations = entityManager.createQuery(criteriaQuery);
        if (foundChatParticipations.getResultList().size() == 0) {
            return null;
        }
        return foundChatParticipations.getSingleResult().getChat();
    }
}
