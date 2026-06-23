package com.cafeuniv.ppi.config;

import org.flywaydb.core.Flyway;

import java.io.InputStream;
import java.util.Properties;

/**
 * Encapsula la ejecución de migraciones Flyway.
 * Lee configuración por perfil y aplica repair+migrate.
 * 
 * USO DE COLECCIONES:
 * - Usa Properties (extends Hashtable<String, String>): Mapa de configuración.
 * - NO usa arreglos ni listas tradicionales.
 */
public final class FlywayMigrator {
    private FlywayMigrator() {}

    /**
     * Ejecuta migraciones de base de datos.
     * Lanza RuntimeException si falla, para detener el arranque.
     */
    public static void migrate() {
        try {
            // MAPA de propiedades de configuración
            Properties props = new Properties();
            try (InputStream is = FlywayMigrator.class.getResourceAsStream("/application.properties")) {
                if (is != null) { props.load(is); }
            }

            // Cargar propiedades del perfil activo si existen
            String activeProfile = props.getProperty("spring.profiles.active", "dev");
            String profileResource = "/application-" + activeProfile + ".properties";
            try (InputStream isProfile = FlywayMigrator.class.getResourceAsStream(profileResource)) {
                if (isProfile != null) {
                    Properties profileProps = new Properties();
                    profileProps.load(isProfile);
                    // FUSIONAR mapas de propiedades
                    props.putAll(profileProps);
                }
            }

            String url = props.getProperty("datasource.jdbcUrl");
            String user = props.getProperty("datasource.username");
            String pass = props.getProperty("datasource.password");
            String locations = props.getProperty("flyway.locations", "classpath:db/migration");

            Flyway flyway = Flyway.configure()
                    .dataSource(url, user, pass)
                    .locations(locations)
                    .baselineOnMigrate(true)
                    .load();
            try {
                flyway.repair();
            } catch (Exception ignored) {}
            flyway.migrate();
        } catch (Exception e) {
            throw new RuntimeException("Error ejecutando migraciones Flyway", e);
        }
    }
}


