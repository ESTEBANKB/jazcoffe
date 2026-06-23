package com.cafeuniv.ppi.repository;

import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.Categoria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Acceso a datos para {@link com.cafeuniv.ppi.domain.Categoria}.
 * 
 * USO DE COLECCIONES:
 * - findAll() retorna List<Categoria>.
 * - NO usa arreglos, solo List del Java Collections Framework.
 */
public class CategoriaRepository {
    /**
     * Retorna LISTA de todas las categorías ordenadas por nombre
     * @return List<Categoria>
     */
    public List<Categoria> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Categoria order by nombre", Categoria.class).list();
        }
    }

    public void saveOrUpdate(Categoria c) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (c.getId() == null) session.persist(c); else session.merge(c);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Categoria c = session.get(Categoria.class, id);
            if (c != null) session.remove(c);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}


