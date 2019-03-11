package com.betpawa.wallet.dao.repository;

import com.betpawa.wallet.dao.entity.Balance;
import com.betpawa.wallet.dao.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;

public class BalanceRepository {

    public List<Balance> findByUserId(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Balance> query = session.createQuery("from Balance where userId = :userId", Balance.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }

    public Balance findByUserIdAndCurrency(int userId, String currency) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            final String hql = "from Balance where userId = :userId and currency = :currency";
            Query<Balance> query = session.createQuery(hql, Balance.class);
            query.setParameter("userId", userId);
            query.setParameter("currency", currency);
            return query.getSingleResult();
        }
    }

    public void save(Balance balance) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(balance);
            session.getTransaction().commit();
        }
    }

    // truncate balance table and generate users balances
    public void init(int numOfUsers) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createSQLQuery("TRUNCATE TABLE balance").executeUpdate();

            final String[] supportedCurrency = {"USD", "EUR", "GBP"};
            for (int id = 1; id <= numOfUsers; id++) {
                for (String currency : supportedCurrency) {
                    Balance balance = new Balance();
                    balance.setUserId(id);
                    balance.setAmount(BigDecimal.ZERO);
                    balance.setCurrency(currency);
                    session.save(balance);
                }
            }

            session.getTransaction().commit();
        }
    }

}
