package com.resways.backend.dao;

import com.resways.backend.model.SurpriseBag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class BagDao {
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("ResWaysPU");

    public SurpriseBag save(SurpriseBag bag) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(bag);
        em.getTransaction().commit();
        em.close();
        return bag;
    }

    public SurpriseBag update(SurpriseBag bag) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        bag = em.merge(bag);
        em.getTransaction().commit();
        em.close();
        return bag;
    }

    public List<SurpriseBag> findAllAvailable() {
        EntityManager em = emf.createEntityManager();
        List<SurpriseBag> bags = em.createQuery("SELECT b FROM SurpriseBag b WHERE b.status = 'Available'", SurpriseBag.class)
                .getResultList();
        em.close();
        return bags;
    }
    
    public List<SurpriseBag> findByStoreId(Long id) {
        EntityManager em = emf.createEntityManager();
        List<SurpriseBag> bags = em.createQuery("SELECT b FROM SurpriseBag b WHERE b.restaurant.id = :id", SurpriseBag.class)
                .setParameter("id", id)
                .getResultList();
        em.close();
        return bags;
    }
    
    public List<SurpriseBag> findByReservedById(Long userId) {
        EntityManager em = emf.createEntityManager();
        List<SurpriseBag> bags = em.createQuery("SELECT b FROM SurpriseBag b WHERE b.reservedById = :userId ORDER BY b.id DESC", SurpriseBag.class)
                .setParameter("userId", userId)
                .getResultList();
        em.close();
        return bags;
    }
    
    public SurpriseBag findById(Long id) {
        EntityManager em = emf.createEntityManager();
        SurpriseBag b = em.find(SurpriseBag.class, id);
        em.close();
        return b;
    }
}
