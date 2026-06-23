package com.cafeuniv.ppi.repository;

import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.Rol;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Acceso a datos para la entidad {@link Rol}.
 */
public class RolRepository {

    public Rol findByNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Rol r where upper(r.nombre) = :n", Rol.class)
                    .setParameter("n", nombre.toUpperCase())
                    .setMaxResults(1)
                    .uniqueResultOptional()
                    .orElse(null);
        }
    }

    public Rol createIfNotExists(String nombre) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Rol existing = session.createQuery("from Rol r where upper(r.nombre) = :n", Rol.class)
                    .setParameter("n", nombre.toUpperCase())
                    .setMaxResults(1)
                    .uniqueResult();
            if (existing != null) {
                tx.commit();
                return existing;
            }
            Rol nuevo = new Rol();
            nuevo.setNombre(nombre.toUpperCase());
            session.persist(nuevo);
            tx.commit();
            return nuevo;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        }
    }
}
