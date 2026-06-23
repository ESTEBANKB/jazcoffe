package com.cafeuniv.ppi.repository;

import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Acceso a datos para {@link com.cafeuniv.ppi.domain.Usuario}.
 * 
 * USO DE COLECCIONES:
 * - findWithRole() retorna List<Usuario>.
 * - Usa HQL queries que retornan listas de resultados.
 * - NO usa arreglos, solo List del Java Collections Framework.
 */
public class UsuarioRepository {
    public Usuario findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> q = session.createQuery("from Usuario u join fetch u.rol where lower(u.email) = :email", Usuario.class);
            q.setParameter("email", email.toLowerCase());
            return q.uniqueResultOptional().orElse(null);
        }
    }

    public void save(Usuario usuario) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(usuario);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Retorna LISTA de todos los usuarios con su rol cargado
     * @return List<Usuario> con todos los usuarios
     */
    public java.util.List<Usuario> findAllWithRole() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Usuario u join fetch u.rol order by u.nombre", Usuario.class).list();
        }
    }

    public void saveOrUpdate(Usuario usuario) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (usuario.getRol() != null && usuario.getRol().getId() == null) {
                // Resolver rol por nombre si solo viene el nombre
                var r = session.createQuery("from Rol r where r.nombre = :n", com.cafeuniv.ppi.domain.Rol.class)
                        .setParameter("n", usuario.getRol().getNombre())
                        .setMaxResults(1)
                        .uniqueResult();
                if (r != null) usuario.setRol(r);
            }
            if (usuario.getId() == null) session.persist(usuario); else session.merge(usuario);
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
            Usuario u = session.get(Usuario.class, id);
            if (u != null) session.remove(u);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void updateRole(Long userId, String roleName) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            var r = session.createQuery("from Rol r where r.nombre = :n", com.cafeuniv.ppi.domain.Rol.class)
                    .setParameter("n", roleName)
                    .setMaxResults(1)
                    .uniqueResult();
            Usuario u = session.get(Usuario.class, userId);
            if (u != null && r != null) {
                u.setRol(r);
                session.merge(u);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void updatePassword(Long userId, String hash) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Usuario u = session.get(Usuario.class, userId);
            if (u != null) {
                u.setHash(hash);
                session.merge(u);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}


