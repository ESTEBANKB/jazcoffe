package com.cafeuniv.ppi.service;

import com.cafeuniv.ppi.domain.Usuario;
import com.cafeuniv.ppi.domain.Rol;
import com.cafeuniv.ppi.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;

/**
 * Servicio de gestión de permisos de usuarios.
 * 
 * USO DE COLECCIONES:
 * - Map<Long, Set<String>> userPermissions: Cache en memoria (legacy, ya no se usa).
 * - Set<String>: Contiene los permisos de cada usuario (sin duplicados).
 * - List<String>: Se usa para listas de permisos disponibles.
 * - Ahora los permisos se almacenan en formato JSON en la base de datos.
 */
public class PermissionService {
    private static PermissionService instance;
    
    private PermissionService() {
        // Constructor vacío - usamos HibernateUtil
    }
    
    public static PermissionService getInstance() {
        if (instance == null) {
            instance = new PermissionService();
        }
        return instance;
    }
    
    /**
     * Convierte Set<String> a JSON array string
     * @param permissions Set de permisos
     * @return String JSON array
     */
    private String setToJson(Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "[]";
        }
        return "[\"" + String.join("\",\"", permissions) + "\"]";
    }
    
    /**
     * Convierte JSON array string a Set<String>
     * @param json JSON array string
     * @return Set de permisos
     */
    private Set<String> jsonToSet(String json) {
        Set<String> result = new HashSet<>();
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
            return result;
        }
        try {
            // Remover [ ] y comillas
            String content = json.trim().replaceAll("^\\[|\\]$", "").replaceAll("\"", "");
            if (!content.isEmpty()) {
                String[] parts = content.split(",");
                result.addAll(Arrays.asList(parts));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * Verifica si el usuario actual tiene un permiso específico.
     * 
     * Lógica de verificación:
     * 1. ADMIN siempre tiene todos los permisos (return true)
     * 2. Si el usuario tiene permisos personalizados en BD, se verifica si tiene ese permiso
     * 3. Si es CAJERO sin permisos personalizados, solo tiene POS_ACCESS y DASHBOARD_VIEW por defecto
     */
    public boolean hasPermission(String permisoNombre) {
        try {
            Usuario currentUser = SessionContext.getCurrentUser();
            if (currentUser == null || currentUser.getRol() == null) {
                return false;
            }
            
            String rolNombre = currentUser.getRol().getNombre();
            
            // 1. ADMIN tiene TODOS los permisos automáticamente
            if ("ADMIN".equals(rolNombre)) {
                return true;
            }
            
            // 2. Cargar permisos personalizados guardados en la BD (campo permisos_json)
            Set<String> customPerms = getCustomPermissions(currentUser.getId());
            
            // 3. Si tiene permisos personalizados, verificar si incluye este permiso
            if (customPerms.contains(permisoNombre)) {
                return true;
            }
            
            // 4. Si es CAJERO sin permisos personalizados, usar permisos por defecto del rol
            if ("CAJERO".equals(rolNombre) && customPerms.isEmpty()) {
                return "POS_ACCESS".equals(permisoNombre) || "DASHBOARD_VIEW".equals(permisoNombre);
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene los permisos personalizados del usuario desde la BD.
     * 
     * Lee el campo permisos_json de la tabla usuarios y lo convierte de JSON a Set<String>.
     * Si el campo está vacío o es null, retorna un Set vacío.
     * 
     * @param userId ID del usuario
     * @return Set de permisos personalizados del usuario
     */
    private Set<String> getCustomPermissions(Long userId) {
        Set<String> result = new HashSet<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Usuario user = session.get(Usuario.class, userId);
            if (user != null && user.getPermisosJson() != null) {
                // Convertir JSON string a Set<String>
                // Ejemplo: ["POS_ACCESS", "INVENTARIO_VIEW"] -> Set con 2 elementos
                result = jsonToSet(user.getPermisosJson());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * Obtiene todos los permisos del usuario actual
     * @return Set<String> de permisos (conjunto sin duplicados)
     */
    public Set<String> getUserPermissions() {
        Set<String> permissions = new HashSet<>();
        try {
            Usuario currentUser = SessionContext.getCurrentUser();
            if (currentUser == null || currentUser.getRol() == null) {
                return permissions;
            }
            
            String rolNombre = currentUser.getRol().getNombre();
            
            if ("ADMIN".equals(rolNombre)) {
                permissions.addAll(java.util.Set.of(
                    "DASHBOARD_VIEW", "POS_ACCESS", "INVENTARIO_VIEW", "INVENTARIO_EDIT",
                    "PRODUCTOS_VIEW", "PRODUCTOS_EDIT", "USUARIOS_VIEW", "USUARIOS_EDIT",
                    "VENTAS_VIEW", "VENTAS_EDIT"
                ));
            } else if ("CAJERO".equals(rolNombre)) {
                // Cargar permisos personalizados
                Set<String> customPerms = getCustomPermissions(currentUser.getId());
                if (!customPerms.isEmpty()) {
                    permissions.addAll(customPerms);
                } else {
                    // Permisos por defecto de CAJERO
                    permissions.addAll(java.util.Set.of("DASHBOARD_VIEW", "POS_ACCESS"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return permissions;
    }
    
    /**
     * Verifica si el usuario puede acceder a un módulo específico
     */
    public boolean canAccessModule(String moduleName) {
        switch (moduleName.toLowerCase()) {
            case "pos":
                return hasPermission("POS_ACCESS");
            case "inventario":
                return hasPermission("INVENTARIO_VIEW") || hasPermission("INVENTARIO_EDIT");
            case "productos":
                return hasPermission("PRODUCTOS_VIEW") || hasPermission("PRODUCTOS_EDIT");
            case "usuarios":
                return hasPermission("USUARIOS_VIEW") || hasPermission("USUARIOS_EDIT");
            case "ventas":
                return hasPermission("VENTAS_VIEW") || hasPermission("VENTAS_EDIT");
            case "dashboard":
                return hasPermission("DASHBOARD_VIEW");
            default:
                return false;
        }
    }
    
    /**
     * Obtiene todos los permisos disponibles
     * @return List<String> de todos los permisos del sistema
     */
    public List<String> getAllPermissions() {
        return List.of(
            "DASHBOARD_VIEW", "POS_ACCESS", "INVENTARIO_VIEW", "INVENTARIO_EDIT",
            "PRODUCTOS_VIEW", "PRODUCTOS_EDIT", "USUARIOS_VIEW", "USUARIOS_EDIT",
            "VENTAS_VIEW", "VENTAS_EDIT"
        );
    }
    
    /**
     * Obtiene los permisos de un usuario específico
     */
    public Set<String> getUserPermissions(Long userId) {
        return getCustomPermissions(userId);
    }
    
    /**
     * Actualiza los permisos de un usuario específico en la base de datos.
     * 
     * IMPORTANTE: Este método SOBRESCRIBE todos los permisos anteriores del usuario.
     * 
     * Proceso:
     * 1. Recibe un Set<String> con los permisos a guardar
     * 2. Convierte el Set a formato JSON: ["POS_ACCESS", "INVENTARIO_VIEW"]
     * 3. Guarda el JSON en el campo permisos_json de la tabla usuarios
     * 
     * @param userId ID del usuario
     * @param permissions Set de permisos a guardar
     * @return true si se guardó correctamente, false si hubo error
     */
    public boolean updateUserPermissions(Long userId, Set<String> permissions) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Usuario user = session.get(Usuario.class, userId);
            if (user != null) {
                // Convertir Set<String> a JSON string
                // Ejemplo: Set con ["POS_ACCESS"] -> '["POS_ACCESS"]'
                String jsonPermisos = setToJson(permissions);
                
                // Guardar en el campo permisos_json
                user.setPermisosJson(jsonPermisos);
                session.merge(user);
                tx.commit();
                return true;
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
