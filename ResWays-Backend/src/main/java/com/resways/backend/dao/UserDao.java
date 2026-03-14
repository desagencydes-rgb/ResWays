package com.resways.backend.dao;

import com.resways.backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class UserDao {
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("ResWaysPU");

    public User save(User user) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.close();
        return user;
    }

    public User findByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        List<User> users = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        em.close();
        return users.isEmpty() ? null : users.get(0);
    }

    public User findById(Long id) {
        EntityManager em = emf.createEntityManager();
        User u = em.find(User.class, id);
        em.close();
        return u;
    }
}
