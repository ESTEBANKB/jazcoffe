package com.cafeuniv.ppi.repository;

import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.MovimientoInventario;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Acceso a datos para {@link com.cafeuniv.ppi.domain.MovimientoInventario}.
 * 
 * USO DE COLECCIONES:
 * - findLast(int limit) retorna List<MovimientoInventario>.
 * - NO usa arreglos, solo List del Java Collections Framework.
 */
public class MovimientoInventarioRepository {
    public void save(MovimientoInventario m) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(m);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Retorna LISTA de los últimos movimientos de inventario
     * @param limit cantidad máxima de registros a retornar
     * @return List<MovimientoInventario> ordenados por fecha descendente
     */
    public List<MovimientoInventario> findLast(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from MovimientoInventario m join fetch m.producto order by m.fecha desc", MovimientoInventario.class)
                    .setMaxResults(limit).list();
        }
    }
}


