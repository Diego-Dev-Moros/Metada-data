package com.metamapa.mapper;

import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.hechos.Multimedia;
import com.metamapa.entities.mongo.ContribuyenteMongo;
import com.metamapa.entities.mongo.HechoMongo;
import com.metamapa.entities.mongo.MultimediaMongo;
import com.metamapa.entities.mongo.UbicacionMongo;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.ubicaciones.Ubicacion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades MongoDB y JPA
 */
@Component
public class MongoEntityMapper {

    /**
     * Convierte HechoMongo (MongoDB) a Hecho (JPA) para enviar al agregador
     */
    public Hecho mongoToJpa(HechoMongo hechoMongo) {
        if (hechoMongo == null) {
            return null;
        }

        Hecho hecho = new Hecho();
        
        // Campos básicos
        if (hechoMongo.getId() != null) {
            hecho.setId(hechoMongo.getId());
        }
        
        hecho.setTitulo(hechoMongo.getTitulo());
        hecho.setDescripcion(hechoMongo.getDescripcion());
        hecho.setCategoria(hechoMongo.getCategoria());
        hecho.setFechaHecho(hechoMongo.getFechaHecho());
        hecho.setFechaCarga(hechoMongo.getFechaCarga());
        hecho.setOrigen(hechoMongo.getOrigen());
        hecho.setEliminado(hechoMongo.isEliminado());
        hecho.setEstadoRevision(hechoMongo.getEstadoRevision());
        hecho.setSugerenciaDeCambio(hechoMongo.getSugerenciaDeCambio());
        hecho.setEsAnonimo(hechoMongo.isEsAnonimo());
        hecho.setContador(hechoMongo.getContador());
        hecho.setFingerprint(hechoMongo.getFingerprint());
        
        // Etiquetas
        if (hechoMongo.getEtiquetas() != null) {
            hecho.setEtiquetas(new ArrayList<>(hechoMongo.getEtiquetas()));
        }
        
        // Fuentes
        if (hechoMongo.getFuentes() != null) {
            hecho.setFuentes(new ArrayList<>(hechoMongo.getFuentes()));
        }
        
        // Ubicación (conversión simple - solo coordenadas por ahora)
        if (hechoMongo.getUbicacion() != null && hechoMongo.getUbicacion().esValida()) {
            Ubicacion ubicacion = new Ubicacion(
                hechoMongo.getUbicacion().getLatitud(),
                hechoMongo.getUbicacion().getLongitud()
            );
            hecho.setUbicacion(ubicacion);
        }
        
        // Contribuyente (conversión simple)
        if (hechoMongo.getContribuyente() != null) {
            Contribuyente contribuyente = new Contribuyente();
            contribuyente.setId(hechoMongo.getContribuyente().getId()); // ¡Faltaba el ID!
            contribuyente.setNombre(hechoMongo.getContribuyente().getNombre());
            if (hechoMongo.getContribuyente().getApellido() != null) {
                contribuyente.setApellido(hechoMongo.getContribuyente().getApellido());
            }
            contribuyente.setFechaNacimiento(hechoMongo.getContribuyente().getFechaNacimiento());
            contribuyente.setFechaRegistro(hechoMongo.getContribuyente().getFechaRegistro());
            contribuyente.setRol(hechoMongo.getContribuyente().getRol());
            hecho.setContribuyente(contribuyente);
        }
        
        // Multimedia
        if (hechoMongo.getMultimedias() != null) {
            List<Multimedia> multimedias = hechoMongo.getMultimedias().stream()
                .map(this::mongoMultimediaToJpa)
                .collect(Collectors.toList());
            hecho.setMultimedias(multimedias);
        }
        
        return hecho;
    }
    
    /**
     * Convierte Hecho (JPA) a HechoMongo (MongoDB) 
     */
    public HechoMongo jpaToMongo(Hecho hecho) {
        if (hecho == null) {
            return null;
        }
        
        HechoMongo hechoMongo = new HechoMongo();
        
        // Campos básicos
        if (hecho.getId() != null) {
            hechoMongo.setId(hecho.getId());
        }
        
        hechoMongo.setTitulo(hecho.getTitulo());
        hechoMongo.setDescripcion(hecho.getDescripcion());
        hechoMongo.setCategoria(hecho.getCategoria());
        hechoMongo.setFechaHecho(hecho.getFechaHecho());
        hechoMongo.setFechaCarga(hecho.getFechaCarga());
        hechoMongo.setOrigen(hecho.getOrigen());
        hechoMongo.setEliminado(hecho.isEliminado());
        hechoMongo.setEstadoRevision(hecho.getEstadoRevision());
        hechoMongo.setSugerenciaDeCambio(hecho.getSugerenciaDeCambio());
        hechoMongo.setEsAnonimo(hecho.isEsAnonimo());
        hechoMongo.setContador(hecho.getContador());
        hechoMongo.setFingerprint(hecho.getFingerprint());
        
        // Listas
        if (hecho.getEtiquetas() != null) {
            hechoMongo.setEtiquetas(new ArrayList<>(hecho.getEtiquetas()));
        }
        if (hecho.getFuentes() != null) {
            hechoMongo.setFuentes(new ArrayList<>(hecho.getFuentes()));
        }
        
        // Ubicación
        if (hecho.getUbicacion() != null) {
            UbicacionMongo ubicacion = new UbicacionMongo(
                hecho.getUbicacion().getLatitud(),
                hecho.getUbicacion().getLongitud()
            );
            hechoMongo.setUbicacion(ubicacion);
        }
        
        // Contribuyente (conversión JPA -> MongoDB)
        if (hecho.getContribuyente() != null) {
            ContribuyenteMongo contribuyenteMongo = new ContribuyenteMongo();
            contribuyenteMongo.setId(hecho.getContribuyente().getId());
            contribuyenteMongo.setNombre(hecho.getContribuyente().getNombre());
            contribuyenteMongo.setApellido(hecho.getContribuyente().getApellido());
            contribuyenteMongo.setFechaNacimiento(hecho.getContribuyente().getFechaNacimiento());
            contribuyenteMongo.setFechaRegistro(hecho.getContribuyente().getFechaRegistro());
            contribuyenteMongo.setRol(hecho.getContribuyente().getRol());
            hechoMongo.setContribuyente(contribuyenteMongo);
        }
        
        return hechoMongo;
    }
    
    private Multimedia mongoMultimediaToJpa(MultimediaMongo multimediaMongo) {
        if (multimediaMongo == null) {
            return null;
        }
        
        Multimedia multimedia = new Multimedia();
        // Mapeo correcto de campos: MultimediaMongo -> Multimedia
        multimedia.setNombre(multimediaMongo.getTitulo()); // titulo -> nombre
        multimedia.setTipo(multimediaMongo.getTipo());
        multimedia.setFormato(multimediaMongo.getFormato());
        multimedia.setTamanio(multimediaMongo.getTamanio() != null ? multimediaMongo.getTamanio() : 0L);
        multimedia.setRuta(multimediaMongo.getUrl()); // url -> ruta
        
        return multimedia;
    }
}