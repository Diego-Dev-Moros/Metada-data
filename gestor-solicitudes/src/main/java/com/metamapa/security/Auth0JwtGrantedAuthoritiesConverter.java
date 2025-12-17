package com.metamapa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Converter personalizado para extraer roles de JWT de Auth0
 * 
 * Auth0 envía los roles/permisos en diferentes claims dependiendo de la configuración:
 * - "permissions": Array de strings con los permisos asignados
 * - "roles": Array de strings con los roles asignados
 * - Custom namespace: ej. "https://metamapa.com/roles"
 * 
 * Este converter busca en múltiples ubicaciones para maximizar compatibilidad
 */
@Slf4j
@Component
public class Auth0JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String ROLES_CLAIM = "roles";
    private static final String NAMESPACE = "https://metamapa.com";
    private static final String ROLE_PREFIX = "ROLE_";
    
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // 1. Buscar en claim "permissions"
        authorities.addAll(extractAuthorities(jwt, PERMISSIONS_CLAIM));
        
        // 2. Buscar en claim "roles"
        authorities.addAll(extractAuthorities(jwt, ROLES_CLAIM));
        
        // 3. Buscar en namespace personalizado
        authorities.addAll(extractAuthorities(jwt, NAMESPACE + "/roles"));
        authorities.addAll(extractAuthorities(jwt, NAMESPACE + "/permissions"));
        
        // Si no se encontraron roles, asignar USER por defecto
        if (authorities.isEmpty()) {
            log.warn("No se encontraron roles en el JWT. Asignando rol USER por defecto");
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + RoleConstants.USER));
        }
        
        log.debug("Roles extraídos del JWT: {}", authorities);
        
        return authorities;
    }
    
    /**
     * Extrae authorities de un claim específico del JWT
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt, String claimName) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        Object claim = jwt.getClaim(claimName);
        
        if (claim == null) {
            return authorities;
        }
        
        // Si es una lista de strings
        if (claim instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> claimList = (List<String>) claim;
            for (String value : claimList) {
                authorities.add(createAuthority(value));
            }
        }
        // Si es un string único
        else if (claim instanceof String) {
            authorities.add(createAuthority((String) claim));
        }
        // Si es un mapa (menos común)
        else if (claim instanceof Map) {
            log.debug("Claim {} es un Map: {}", claimName, claim);
        }
        
        return authorities;
    }
    
    /**
     * Crea una GrantedAuthority asegurando el prefijo ROLE_
     */
    private GrantedAuthority createAuthority(String role) {
        String normalizedRole = role.toUpperCase().trim();
        
        // Si ya tiene el prefijo ROLE_, no agregarlo de nuevo
        if (normalizedRole.startsWith(ROLE_PREFIX)) {
            return new SimpleGrantedAuthority(normalizedRole);
        }
        
        // Agregar prefijo ROLE_
        return new SimpleGrantedAuthority(ROLE_PREFIX + normalizedRole);
    }
}
