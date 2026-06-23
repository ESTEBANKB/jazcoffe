package com.cafeuniv.ppi.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.io.InputStream;
import java.util.Properties;

/**
 * Proveedor único de {@link org.hibernate.SessionFactory}.
 *
 * Responsabilidades:
 * - Cargar propiedades por perfil (dev/prod), con overrides por env/props.
 * - Configurar Hibernate + HikariCP y registrar entidades.
 * 
 * USO DE COLECCIONES:
 * - Usa Properties (extends Hashtable<String, String>): Mapa de configuración.
 * - NO usa arreglos ni listas tradicionales.
 * - Registra clases entidad individualmente con addAnnotatedClass().
 */
public final class HibernateUtil {
    private static SessionFactory sessionFactory;

    private HibernateUtil() {}

    /**
     * Obtiene una SessionFactory singleton inicializada bajo demanda.
     */
    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                // MAPA de propiedades de configuración
                Properties props = new Properties();
                try (InputStream is = HibernateUtil.class.getResourceAsStream("/application.properties")) {
                    if (is != null) {
                        props.load(is);
                    }
                }

                // Determinar perfil activo: env > system prop > archivo
                String envProfile = System.getenv("SPRING_PROFILES_ACTIVE");
                String sysProfile = System.getProperty("spring.profiles.active");
                String activeProfile = envProfile != null && !envProfile.isBlank()
                        ? envProfile
                        : (sysProfile != null && !sysProfile.isBlank()
                            ? sysProfile
                            : props.getProperty("spring.profiles.active", "dev"));
                String profileResource = "/application-" + activeProfile + ".properties";
                try (InputStream isProfile = HibernateUtil.class.getResourceAsStream(profileResource)) {
                    if (isProfile != null) {
                        Properties profileProps = new Properties();
                        profileProps.load(isProfile);
                        // FUSIONAR mapas de propiedades
                        props.putAll(profileProps);
                    }
                }

                // Permitir overrides por variables de entorno (estilo ${ENV:default})
                props.replaceAll((k, v) -> {
                    if (v instanceof String s) {
                        int start = s.indexOf("${");
                        int end = s.indexOf("}");
                        if (start >= 0 && end > start) {
                            String expr = s.substring(start + 2, end);
                            String[] parts = expr.split(":", 2);
                            String envName = parts[0];
                            String def = parts.length > 1 ? parts[1] : "";
                            String envVal = System.getenv(envName);
                            String replaced = envVal != null && !envVal.isEmpty() ? envVal : def;
                            return s.substring(0, start) + replaced + s.substring(end + 1);
                        }
                    }
                    return v;
                });

                Configuration configuration = new Configuration();
                configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
                configuration.setProperty("hibernate.hikari.jdbcUrl", props.getProperty("datasource.jdbcUrl"));
                configuration.setProperty("hibernate.hikari.username", props.getProperty("datasource.username"));
                configuration.setProperty("hibernate.hikari.password", props.getProperty("datasource.password"));
                configuration.setProperty("hibernate.hikari.maximumPoolSize", props.getProperty("datasource.pool.size", "10"));
                configuration.setProperty("hibernate.dialect", props.getProperty("hibernate.dialect"));
                configuration.setProperty("hibernate.hbm2ddl.auto", props.getProperty("hibernate.hbm2ddl.auto", "none"));
                configuration.setProperty("hibernate.show_sql", props.getProperty("hibernate.show_sql", "false"));
                configuration.setProperty("hibernate.format_sql", props.getProperty("hibernate.format_sql", "true"));

                // REGISTRO individual de clases entidad (no hay lista para iterar)
                configuration.addAnnotatedClass(com.cafeuniv.ppi.domain.Rol.class);
                configuration.addAnnotatedClass(com.cafeuniv.ppi.domain.Usuario.class);
                configuration.addAnnotatedClass(com.cafeuniv.ppi.domain.Categoria.class);
                configuration.addAnnotatedClass(com.cafeuniv.ppi.domain.Producto.class);
                configuration.addAnnotatedClass(com.cafeuniv.ppi.domain.MovimientoInventario.class);
                configuration.addAnnotatedClass(com.cafeuniv.ppi.domain.Venta.class);
                configuration.addAnnotatedClass(com.cafeuniv.ppi.domain.VentaDetalle.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                throw new RuntimeException("Error inicializando SessionFactory", e);
            }
        }
        return sessionFactory;
    }
}


