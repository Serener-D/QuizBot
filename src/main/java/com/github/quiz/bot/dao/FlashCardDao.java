package com.github.quiz.bot.dao;

import com.github.quiz.bot.config.DatabaseConfig;
import com.github.quiz.bot.entity.FlashCard;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

@Slf4j
public class FlashCardDao {

    private static final SessionFactory sessionFactory = DatabaseConfig.getSessionFactory();

    public static void save(FlashCard flashCard) {
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

    public static void delete(Long cardId) {
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

    public static List<FlashCard> getAllByChatId(Long chatId) {
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

    public static FlashCard get(Long cardId) {
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

    public static List<FlashCard> getLeastUsedByChatId(Long chatId) {
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
                    .setMaxResults(20);
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

    public static List<FlashCard> getLeastUsedByChatId(Long chatId, String category) {
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
                    .setMaxResults(20);
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
