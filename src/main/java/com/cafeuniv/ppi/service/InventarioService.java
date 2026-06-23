package com.cafeuniv.ppi.service;

import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.MovimientoInventario;
import com.cafeuniv.ppi.domain.Producto;
import org.hibernate.Session;
import org.hibernate.LockOptions;
import org.hibernate.Transaction;

/**
 * Servicio de inventario: entradas y salidas.
 */
public class InventarioService {
    /**
     * Registra un movimiento de inventario validando stock y tipo.
     * @param productoId id del producto
     * @param tipo "entrada" o "salida"
     * @param cantidad unidades a mover (> 0)
     * @param motivo descripción del movimiento
     * @param usuarioId id del usuario que realiza la acción
     */
    public void registrarMovimiento(Long productoId, String tipo, int cantidad, String motivo, Long usuarioId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
            Producto p = session.get(Producto.class, productoId, LockOptions.UPGRADE);
            if (p == null) throw new IllegalArgumentException("Producto no encontrado");
            int nuevo = p.getStockActual();
            if ("entrada".equalsIgnoreCase(tipo)) {
                nuevo += cantidad;
            } else if ("salida".equalsIgnoreCase(tipo)) {
                if (nuevo < cantidad) throw new IllegalStateException("Stock insuficiente");
                nuevo -= cantidad;
            } else {
                throw new IllegalArgumentException("Tipo de movimiento inválido");
            }
            p.setStockActual(nuevo);
            session.merge(p);
            MovimientoInventario m = new MovimientoInventario();
            m.setProducto(p);
            m.setTipo(tipo);
            m.setCantidad(cantidad);
            m.setMotivo(motivo);
            m.setUsuarioId(usuarioId);
            session.persist(m);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}


