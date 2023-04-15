package com.github.quiz.bot.dao;

import com.github.quiz.bot.entity.FlashCard;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FlashCardDao {

    private final SessionFactory sessionFactory;

    public void save(FlashCard flashCard) {
        Transaction transaction = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();
            session.persist(flashCard);
            transaction.commit();
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public void update(FlashCard flashCard) {
        Transaction transaction = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();
            session.merge(flashCard);
            transaction.commit();
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public void delete(Long cardId) {
        Transaction transaction = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();
            session.createQuery("DELETE FROM FlashCard c WHERE c.id=:id")
                    .setParameter("id", cardId)
                    .executeUpdate();
            transaction.commit();
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<FlashCard> getAllByChatId(Long chatId) {
        Transaction transaction = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();
            TypedQuery<FlashCard> typedQuery = session
                    .createQuery("SELECT c FROM FlashCard c WHERE c.chatId=:chatId", FlashCard.class)
                    .setParameter("chatId", chatId);
            List<FlashCard> cards = typedQuery.getResultList();
            transaction.commit();
            return cards;
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<String> getAllCategoriesByChatId(Long chatId) {
        Transaction transaction = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();
            TypedQuery<String> typedQuery = session
                    .createQuery("SELECT DISTINCT(c.category) FROM FlashCard c WHERE c.chatId=:chatId", String.class)
                    .setParameter("chatId", chatId);
            List<String> categories = typedQuery.getResultList();
            transaction.commit();
            return categories;
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public FlashCard get(Long cardId) {
        Transaction transaction = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();
            Query<FlashCard> query = sessionFactory.getCurrentSession()
                    .createQuery("SELECT c FROM FlashCard c WHERE c.id=:cardId", FlashCard.class)
                    .setParameter("cardId", cardId);
            FlashCard card = query.getSingleResult();
            transaction.commit();
            return card;
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<FlashCard> getLeastUsedByChatId(Long chatId, int limit) {
        Transaction transaction = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();
            TypedQuery<FlashCard> typedQuery = session
                    .createQuery("SELECT c " +
                            "FROM FlashCard c " +
                            "WHERE c.chatId=:chatId " +
                            "ORDER BY c.showedCounter ASC", FlashCard.class)
                    .setParameter("chatId", chatId)
                    .setMaxResults(limit);
            List<FlashCard> cards = typedQuery.getResultList();
            transaction.commit();
            return cards;
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<FlashCard> getLeastUsedByChatIdAndCategory(Long chatId, String category, int limit) {
        Transaction transaction = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.getTransaction();
            transaction.begin();
            TypedQuery<FlashCard> typedQuery = session
                    .createQuery("SELECT c " +
                            "FROM FlashCard c " +
                            "WHERE c.chatId=:chatId AND c.category=:category " +
                            "ORDER BY c.showedCounter ASC", FlashCard.class)
                    .setParameter("chatId", chatId)
                    .setParameter("category", category)
                    .setMaxResults(limit);
            List<FlashCard> cards = typedQuery.getResultList();

            transaction.commit();
            return cards;
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }


}
