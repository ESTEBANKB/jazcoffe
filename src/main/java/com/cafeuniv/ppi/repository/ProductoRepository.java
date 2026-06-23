package com.cafeuniv.ppi.repository;

import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.Producto;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Acceso a datos para {@link com.cafeuniv.ppi.domain.Producto}.
 * 
 * USO DE COLECCIONES:
 * - findAll() retorna List<Producto>.
 * - NO usa arreglos, solo List del Java Collections Framework.
 */
public class ProductoRepository {
    /**
     * Retorna LISTA de todos los productos con su categoría cargada
     * @return List<Producto> ordenados por nombre
     */
    public List<Producto> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Producto p left join fetch p.categoria order by p.nombre", Producto.class).list();
        }
    }

    public void saveOrUpdate(Producto p) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("=== REPOSITORIO: Guardando producto ===");
            System.out.println("Producto: " + p.getNombre());
            System.out.println("Categoría: " + (p.getCategoria() != null ? p.getCategoria().getNombre() : "null"));
            
            tx = session.beginTransaction();
            if (p.getId() == null) {
                System.out.println("Persistiendo nuevo producto...");
                session.persist(p);
            } else {
                System.out.println("Actualizando producto existente...");
                session.merge(p);
            }
            tx.commit();
            System.out.println("Transacción completada. ID asignado: " + p.getId());
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("ERROR en repositorio: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("=== REPOSITORIO: Eliminando producto ===");
            System.out.println("ID a eliminar: " + id);
            
            tx = session.beginTransaction();
            Producto p = session.get(Producto.class, id);
            if (p != null) {
                System.out.println("Producto encontrado: " + p.getNombre());
                session.remove(p);
                System.out.println("Producto marcado para eliminación");
            } else {
                System.out.println("Producto no encontrado con ID: " + id);
            }
            tx.commit();
            System.out.println("Transacción de eliminación completada");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("ERROR en repositorio al eliminar: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Producto findById(Long id) {
        if (id == null) return null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Producto.class, id);
        }
    }

    /**
     * Busca un producto por nombre (case-insensitive).
     * @param nombre Nombre del producto a buscar
     * @return Producto encontrado o null si no existe
     */
    public Producto findByNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "from Producto p where lower(trim(p.nombre)) = lower(trim(:nombre))", 
                Producto.class
            )
            .setParameter("nombre", nombre)
            .uniqueResult();
        }
    }
}


