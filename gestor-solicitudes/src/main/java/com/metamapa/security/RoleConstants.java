package com.metamapa.security;

/**
 * Constantes para los roles de usuario en el sistema MetaMapa
 * 
 * Estos roles se mapean desde Auth0 y se utilizan para controlar el acceso
 * a las diferentes APIs y endpoints del sistema.
 * 
 * @author MetaMapa Team
 */
public class RoleConstants {
    
    /**
     * Usuario normal (Visualizador)
     * - Puede ver hechos y colecciones
     * - Puede filtrar y buscar información
     * - Puede solicitar eliminaciones
     * - NO puede crear o editar contenido
     */
    public static final String USER = "USER";
    
    /**
     * Usuario Contribuyente
     * - Todos los permisos de USER
     * - Puede crear nuevos hechos
     * - Puede editar su propio perfil
     * - Puede editar sus propios hechos (dentro de 7 días)
     */
    public static final String CONTRIBUTOR = "CONTRIBUTOR";
    
    /**
     * Administrador
     * - Todos los permisos de CONTRIBUTOR
     * - Acceso completo a la API Administrativa
     * - Puede gestionar colecciones
     * - Puede aprobar/rechazar hechos
     * - Puede gestionar solicitudes de eliminación
     * - Puede modificar cualquier contenido
     */
    public static final String ADMIN = "ADMIN";
    
    // Expresiones para Spring Security @PreAuthorize
    public static final String HAS_ROLE_USER = "hasRole('" + USER + "')";
    public static final String HAS_ROLE_CONTRIBUTOR = "hasRole('" + CONTRIBUTOR + "')";
    public static final String HAS_ROLE_ADMIN = "hasRole('" + ADMIN + "')";
    
    public static final String HAS_ROLE_USER_OR_CONTRIBUTOR = "hasAnyRole('" + USER + "', '" + CONTRIBUTOR + "')";
    public static final String HAS_ROLE_CONTRIBUTOR_OR_ADMIN = "hasAnyRole('" + CONTRIBUTOR + "', '" + ADMIN + "')";
    public static final String HAS_ANY_ROLE = "hasAnyRole('" + USER + "', '" + CONTRIBUTOR + "', '" + ADMIN + "')";
    
    private RoleConstants() {
        // Evitar instanciación
    }
}
