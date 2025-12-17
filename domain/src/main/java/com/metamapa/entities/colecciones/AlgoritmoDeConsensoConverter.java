package com.metamapa.entities.colecciones;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Persiste la interfaz AlgoritmoDeConsenso como un VARCHAR (token).
 * Es stateless: las clases concretas no tienen atributos.
 */
@Converter(autoApply = true)
public class AlgoritmoDeConsensoConverter
        implements AttributeConverter<AlgoritmoDeConsenso, String> {

    // Tokens que van a la BD (podés cambiarlos si preferís)
    public static final String ABSOLUTA = "absoluta";
    public static final String MAYORIA_SIMPLE = "mayoria_simple";
    public static final String MULTIPLES_MENCIONES = "multiples_menciones";
    public static final String POR_DEFECTO = "por_defecto";

    private static final Map<String, Supplier<AlgoritmoDeConsenso>> FROM_DB = new HashMap<>();
    static {
        FROM_DB.put(ABSOLUTA, Absoluta::new);
        FROM_DB.put(MAYORIA_SIMPLE, MayoriaSimple::new);
        FROM_DB.put(MULTIPLES_MENCIONES, MultiplesMenciones::new);
        FROM_DB.put(POR_DEFECTO, PorDefecto::new);
    }

    @Override
    public String convertToDatabaseColumn(AlgoritmoDeConsenso algoritmo) {
        if (algoritmo == null) return null;
        if (algoritmo instanceof Absoluta) return ABSOLUTA;
        if (algoritmo instanceof MayoriaSimple) return MAYORIA_SIMPLE;
        if (algoritmo instanceof MultiplesMenciones) return MULTIPLES_MENCIONES;
        if (algoritmo instanceof PorDefecto) return POR_DEFECTO;
        // Fallback seguro
        throw new IllegalArgumentException("AlgoritmoDeConsenso desconocido: " + algoritmo.getClass());
    }

    @Override
    public AlgoritmoDeConsenso convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        Supplier<AlgoritmoDeConsenso> sup = FROM_DB.get(dbData);
        if (sup == null)
            throw new IllegalArgumentException("Token de algoritmo desconocido: " + dbData);
        return sup.get(); // instancia stateless
    }
}
