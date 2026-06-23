package com.cafeuniv.ppi.repository;

import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.Venta;
import org.hibernate.Session;

/**
 * Acceso a datos para {@link com.cafeuniv.ppi.domain.Venta}.
 * 
 * USO DE COLECCIONES:
 * Este repositorio actualmente NO retorna listas, solo guarda ventas individuales.
 * En el futuro podría tener métodos como findAll(), findByFecha(), etc.
 * que retornarían List<Venta>.
 */
public class VentaRepository {
    /**
     * Guarda una venta individual en la base de datos
     * @param venta la venta a persistir
     */
    public void save(Venta venta) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            s.beginTransaction();
            s.persist(venta);
            s.getTransaction().commit();
        }
    }
}


