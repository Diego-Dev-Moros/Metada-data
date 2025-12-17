package com.metamapa.service.normalizacion;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementación simple de CategoriaResolverTaxonomico.
 * Retorna la categoría original si existe, sino "otros/desconocido".
 *
 * Activa por defecto (perfil 'default' o 'prod').
 *
 */
@Component
@Profile({"default", "prod"})  // se activa cuando no usás perfil demo
public class CategoriaResolverStub implements CategoriaResolverTaxonomico {

    public static final String OTROS = "otros/desconocido";

    @Override
    public String clasificar(String titulo, String descripcion, String categoriaOriginal) {
        // Si viene categoría original de la fuente, usarla
        if (categoriaOriginal != null && !categoriaOriginal.trim().isEmpty()) {
            return categoriaOriginal.trim().toLowerCase();
        }
        // Si no, clasificar por defecto
        return OTROS;
    }
}