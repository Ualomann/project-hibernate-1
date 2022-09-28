package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;


    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.HBM2DDL_AUTO,"update");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
//        properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
//        properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "password");
//        properties.put(Environment.SHOW_SQL, "true");
//        properties.put(Environment.FORMAT_SQL, "true");
//        properties.put(Environment.HIGHLIGHT_SQL, "true");
//        properties.put(Environment.USE_SQL_COMMENTS, "true");
        sessionFactory = new Configuration()
                .addAnnotatedClass(Player.class)
                .addProperties(properties)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try(Session session = sessionFactory.openSession()) {
            NativeQuery<Player> query = session.createNativeQuery("SELECT * FROM rpg.player", Player.class)
                    .setFirstResult(pageNumber * pageSize)
                    .setMaxResults(pageSize);
            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()) {
            Query <Long> query = session.createNamedQuery("player_getAllCounts", Long.class);
            return Math.toIntExact(query.uniqueResult());
        }
    }

    @Override
    public Player save(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.persist(player);

            transaction.commit();
            return player;
        }
    }

    @Override
    public Player update(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.update(player);
            transaction.commit();
            return player;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
           Player player = session.get(Player.class, id);
           return Optional.ofNullable(player);
        }
    }

    @Override
    public void delete(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.delete(player);
            transaction.commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        try(Session session = sessionFactory.openSession()) {
            sessionFactory.close();
        }
    }
}