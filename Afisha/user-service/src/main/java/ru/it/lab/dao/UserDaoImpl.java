package ru.it.lab.dao;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.it.lab.entitities.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Component
public class UserDaoImpl extends AbstractDaoImpl<User> implements UserDao {

    @PersistenceContext
    @Getter(AccessLevel.PROTECTED)
    private EntityManager entityManager;

    @Override
    public User getByUsername(String username) {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<User> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<User> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(
                        rootEntry.get("username"),
                        username)
                );
        TypedQuery<User> foundUsers = entityManager.createQuery(crit);
        if (foundUsers.getResultList().size() == 0) {
            return null;
        }
        return foundUsers.getSingleResult();
    }

    @Override
    public User getByActivationCode(String activationCode) {
        Class entityClass = User.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<User> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<User> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(
                        rootEntry.get("activationCode"),
                        activationCode)
                );
        TypedQuery<User> foundUsers = entityManager.createQuery(crit);
        if (foundUsers.getResultList().size() == 0) {
            return null;
        }
        return foundUsers.getSingleResult();
    }

    @Override
    public User getByResetCode(String code) {
        Class entityClass = User.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<User> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<User> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(
                        rootEntry.get("restorePasswordCode"),
                        code)
                );
        TypedQuery<User> foundUsers = entityManager.createQuery(crit);
        if (foundUsers.getResultList().size() == 0) {
            return null;
        }
        return foundUsers.getSingleResult();
    }
}
