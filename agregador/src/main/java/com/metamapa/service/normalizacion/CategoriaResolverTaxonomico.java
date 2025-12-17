package com.metamapa.service.normalizacion;

public interface  CategoriaResolverTaxonomico {
    /**
     * @param titulo             título normalizado del hecho
     * @param descripcion        descripción (puede ser null)
     * @param categoriaOriginal  etiqueta original que mandó la fuente (puede ser null)
     * @return ruta canónica de la taxonomía
     */
    String clasificar(String titulo, String descripcion, String categoriaOriginal);
}
