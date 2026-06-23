package com.cafeuniv.ppi;

import com.cafeuniv.ppi.config.FlywayMigrator;
import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.Rol;
import com.cafeuniv.ppi.domain.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Inicialización del entorno de la aplicación.
 *
 * Responsabilidades:
 * - Ejecutar migraciones de base de datos con Flyway.
 * - Sembrar/asegurar datos mínimos (roles y usuario admin).
 * 
 * NOTA SOBRE COLECCIONES:
 * Este archivo NO usa arreglos ni listas directamente. Solo crea objetos
 * individuales (Rol, Usuario) y los persiste en la base de datos.
 */
public final class Bootstrap {
    private Bootstrap() {}

    /**
     * Ejecuta migraciones y siembra datos iniciales.
     */
    public static void init() {
        FlywayMigrator.migrate();
        seedAdmin();
    }

    /**
     * Crea roles por defecto y garantiza un usuario administrador activo.
     */
    private static void seedAdmin() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Long rolesCount = session.createQuery("select count(r) from Rol r", Long.class).uniqueResult();
            if (rolesCount == 0) {
                Rol admin = new Rol(); admin.setNombre("ADMIN");
                Rol cajero = new Rol(); cajero.setNombre("CAJERO");
                session.persist(admin); session.persist(cajero);
            }
            Usuario existing = session.createQuery("from Usuario u where lower(u.email) = :e", Usuario.class)
                    .setParameter("e", "admin@local").uniqueResult();
            Rol adminRol = session.createQuery("from Rol r where r.nombre = :n", Rol.class)
                    .setParameter("n", "ADMIN").setMaxResults(1).uniqueResult();
            if (existing == null) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setEmail("admin@local");
                admin.setHash(BCrypt.withDefaults().hashToString(10, "admin".toCharArray()));
                admin.setRol(adminRol);
                admin.setActivo(true);
                session.persist(admin);
            } else {
                existing.setRol(adminRol);
                existing.setActivo(true);
                existing.setHash(BCrypt.withDefaults().hashToString(10, "admin".toCharArray()));
                session.merge(existing);
            }
            tx.commit();
        }
    }
}


