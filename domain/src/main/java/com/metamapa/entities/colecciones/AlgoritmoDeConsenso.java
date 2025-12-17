package com.metamapa.entities.colecciones;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.metamapa.entities.hechos.Hecho;
import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipo"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Absoluta.class, name = "absoluta"),
    @JsonSubTypes.Type(value = MayoriaSimple.class, name = "mayoria_simple"),
    @JsonSubTypes.Type(value = MultiplesMenciones.class, name = "multiples_menciones"),
    @JsonSubTypes.Type(value = PorDefecto.class, name = "por_defecto")
})
public interface AlgoritmoDeConsenso {
    List<Hecho> filtrarConsensuados(List<Hecho> hechos, List<List<Hecho>> hechosPorFuente);
}