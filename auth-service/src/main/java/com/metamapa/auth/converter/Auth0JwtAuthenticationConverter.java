package com.metamapa.auth.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converter que extrae los roles del JWT de Auth0 y los convierte
 * a GrantedAuthority de Spring Security
 * 
 * Auth0 incluye los roles en un claim personalizado con namespace,
 * por ejemplo: "https://metamapa.com/roles": ["ADMIN", "USER"]
 * 
 * Este converter:
 * 1. Extrae el claim "https://metamapa.com/roles" del JWT
 * 2. Convierte cada rol al formato de Spring Security: ROLE_ADMIN, ROLE_USER
 * 3. Crea las GrantedAuthority necesarias
 * 
 * Esto permite usar anotaciones como:
 * - @PreAuthorize("hasRole('ADMIN')")
 * - @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
 * 
 * @author MetaMapa Team
 */
@Component
public class Auth0JwtAuthenticationConverter 
        implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * Namespace del claim de roles configurado en Auth0 Action
     * Debe coincidir con el namespace usado en el Action de Auth0
     */
    private static final String ROLES_CLAIM = "https://metamapa.com/roles";
    
    /**
     * Prefijo requerido por Spring Security para roles
     * Spring Security espera que los roles tengan el prefijo "ROLE_"
     */
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Convierte un JWT en un Authentication token con roles
     * 
     * @param jwt El token JWT de Auth0
     * @return AbstractAuthenticationToken con las autoridades (roles) extraídas
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extrae las autoridades (roles) del JWT
     * 
     * Ejemplo de JWT:
     * {
     *   "https://metamapa.com/roles": ["ADMIN", "USER"],
     *   "sub": "auth0|123456",
     *   "email": "usuario@ejemplo.com"
     * }
     * 
     * Se convierte a:
     * [ROLE_ADMIN, ROLE_USER]
     * 
     * @param jwt El token JWT
     * @return Collection de GrantedAuthority
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Obtener el claim de roles (puede ser null si el usuario no tiene roles)
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        
        // Si no hay roles, devolver lista vacía
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        // Convertir cada rol a GrantedAuthority con prefijo ROLE_
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toList());
    }
}
