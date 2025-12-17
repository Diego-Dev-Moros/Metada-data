package com.metamapa.entities.colecciones;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.MetodoDeNavegacion;
import com.metamapa.entities.criterioDePertenencia.Criterio;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.ubicaciones.Lugar;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table (name = "Coleccion")
public class Coleccion {

    // Getters y setters
    //private String titulo;
    //private String descripcion;

    //@Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    //@Column(name = "identificador", length = 36)
    //private UUID identificador;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long identificador;

    @Column(nullable = false, unique = true, length = 100)
    private String identificadorPublico; // handle público (requerido por el TP)

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(length = 2000)
    private String descripcion;

    @Convert(converter = AlgoritmoDeConsensoConverter.class)
    @Column(name = "algoritmo_consenso", nullable = false, length = 50)
    private AlgoritmoDeConsenso algoritmoDeConsenso;

    @ManyToOne
    @JoinColumn(name = "id_contribuyente", nullable = false)
    private Contribuyente administrador;

    @Column(nullable = false)
    private boolean oculta = false; // Para soft delete - no eliminar físicamente

    @Column(name = "fecha_ultima_modificacion")
    private java.time.LocalDateTime fechaUltimaModificacion;
    
    @Column(name = "contador_visitas", nullable = false)
    private int contadorVisitas = 0;

    // Relaciones persistentes
    @JsonIgnore
    @OneToMany(mappedBy = "coleccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColeccionFuente> fuentesPersistentes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "coleccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColeccionHecho> hechosPersistentes = new ArrayList<>();

    // Exponer criterios para la API - se serializan correctamente
    @JsonProperty("criterios")
    @OneToMany(mappedBy = "coleccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColeccionCriterio> criteriosPersistentes = new ArrayList<>();

    // Campos transitorios (no se guardan en BD)
    @Transient private List<FuenteDeDatos> fuentes = new ArrayList<>();
    @Transient private Map<Hecho, Boolean> hechos = new HashMap<>();
    @JsonIgnore // Ignorar el campo transitorio de criterios, usar criteriosPersistentes
    @Transient private List<Criterio> criterios = new ArrayList<>();
    
   /* @OneToMany(mappedBy = "coleccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColeccionFuente> fuentesPersistentes = new ArrayList<>();

    @OneToMany(mappedBy = "coleccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColeccionHecho> hechosPersistentes = new ArrayList<>();

    @OneToMany(mappedBy = "coleccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColeccionCriterio> criteriosPersistentes = new ArrayList<>();

    // Campos transitorios para compatibilidad con el código existente
    @Transient // No persiste en base de datos - se cargan desde las entidades relacionadas
    private List<FuenteDeDatos> fuentes = new ArrayList<>();

    // Map donde la clave es el Hecho y el valor indica si está consensuado
    @JsonIgnore // No serializar directamente el Map interno
    @Transient // No persiste en base de datos - se carga desde hechosPersistentes
    private Map<Hecho, Boolean> hechos = new HashMap<>();

    @Transient // No persiste en base de datos - se cargan desde criteriosPersistentes
    private List<Criterio> criterios = new ArrayList<>();

    @Convert(converter = AlgoritmoDeConsensoConverter.class)
    @Column(name = "algoritmo_consenso", nullable = false, length = 50)
    private AlgoritmoDeConsenso algoritmoDeConsenso;


    @ManyToOne
    @JoinColumn(name = "id_contribuyente", referencedColumnName = "id")
    private Contribuyente administrador;

    // Propiedades para la serialización JSON
    @JsonProperty("hechos")
    public List<Hecho> getHechosParaJson() {
        return new ArrayList<>(hechos.keySet());
    }

    @JsonProperty("hechosConsensuados")
    public List<Hecho> getHechosConsensuadosParaJson() {
        return verHechosConsensuados();
    }*/

    // ==================== Métodos para cargar datos desde entidades persistentes ====================

    @PostLoad
    private void cargarDatosTransitorios() {
        if (hechosPersistentes != null) {
            hechos.clear();
            for (ColeccionHecho ch : hechosPersistentes) {
                hechos.put(ch.getHecho(), ch.getConsensuado());
            }
        }
        if (criteriosPersistentes != null) {
            criterios.clear();
            for (ColeccionCriterio cc : criteriosPersistentes) {
                Criterio criterio = cc.toCriterio();
                if (criterio != null) criterios.add(criterio);
            }
        }
        //cargarHechosDesdeEntidadesPersistentes();
        //cargarCriteriosDesdeEntidadesPersistentes();
    }

    private void cargarHechosDesdeEntidadesPersistentes() {
        hechos.clear();
        for (ColeccionHecho ch : hechosPersistentes) {
            hechos.put(ch.getHecho(), ch.getConsensuado());
        }
    }

    private void cargarCriteriosDesdeEntidadesPersistentes() {
        criterios.clear();
        for (ColeccionCriterio cc : criteriosPersistentes) {
            Criterio criterio = crearCriterioDesdeEntidad(cc);
            if (criterio != null) {
                criterios.add(criterio);
            }
        }
    }

    /**
     * Factory method mejorado para crear criterios desde entidades persistentes
     * Este método maneja todos los tipos de criterios existentes
     */

    private Criterio crearCriterioDesdeEntidad(ColeccionCriterio cc) {
        // Factory method para crear criterios desde entidades persistentes
        switch (cc.getTipoCriterio()) {
            case "CATEGORIA":
                return hecho -> hecho.getCategoria() != null && hecho.getCategoria().equalsIgnoreCase(cc.getValor());
            case "TITULO":
                return hecho -> hecho.getTitulo() != null && hecho.getTitulo().toLowerCase().contains(cc.getValor().toLowerCase());
            case "DESCRIPCION":
                return hecho -> hecho.getDescripcion() != null && hecho.getDescripcion().toLowerCase().contains(cc.getValor().toLowerCase());
            case "FECHA_CARGA":
                return hecho -> {
                    if (hecho.getFechaCarga() == null || cc.getFechaDesde() == null || cc.getFechaHasta() == null) {
                        return false;
                    }
                    LocalDateTime fecha = hecho.getFechaCarga();
                    return (fecha.isEqual(cc.getFechaDesde()) || fecha.isAfter(cc.getFechaDesde())) &&
                           (fecha.isEqual(cc.getFechaHasta()) || fecha.isBefore(cc.getFechaHasta()));
                };
            case "FECHA_ACONTECIMIENTO":
                return hecho -> {
                    if (hecho.getFechaHecho() == null || cc.getFechaDesde() == null || cc.getFechaHasta() == null) {
                        return false;
                    }
                    LocalDateTime fecha = hecho.getFechaHecho();
                    return (fecha.isEqual(cc.getFechaDesde()) || fecha.isAfter(cc.getFechaDesde())) &&
                           (fecha.isEqual(cc.getFechaHasta()) || fecha.isBefore(cc.getFechaHasta()));
                };
            case "UBICACION":
                return hecho -> {
                    if (hecho.getUbicacion() == null) {
                        return false;
                    }
                    try {
                        Lugar lugarHecho = hecho.getUbicacion().convertirALugar();

                        if (cc.getPais() != null && !cc.getPais().equalsIgnoreCase(lugarHecho.getPais())) {
                            return false;
                        }
                        if (cc.getProvincia() != null && !cc.getProvincia().equalsIgnoreCase(lugarHecho.getProvincia())) {
                            return false;
                        }
                        if (cc.getMunicipio() != null && !cc.getMunicipio().equalsIgnoreCase(lugarHecho.getMunicipio())) {
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        return false; // Si hay error al convertir ubicación
                    }
                };
            default:
                return null;
        }
    }

    // Métodos para sincronizar datos transitorios a entidades persistentes
    public void agregarHecho(Hecho hecho, Boolean consensuado) {
        ColeccionHecho ch = new ColeccionHecho(this, hecho, consensuado);
        hechosPersistentes.add(ch);
        hechos.put(hecho, consensuado);
    }

    public void agregarCriterio(ColeccionCriterio criterio) {
        criterio.setColeccion(this);
        criteriosPersistentes.add(criterio);
        cargarCriteriosDesdeEntidadesPersistentes();
    }

    public Coleccion(String titulo, String descripcion, Long identificador) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.identificador = identificador;
    }

    public Coleccion(String titulo, String descripcion, Long identificador,
                     List<FuenteDeDatos> fuentes, AlgoritmoDeConsenso algoritmo) {
        this(titulo, descripcion, identificador);
        this.fuentes = fuentes;
        this.algoritmoDeConsenso = algoritmo;
    }

    public Coleccion(String titulo, String descripcion, List<Criterio> criterios, Long identificador) {
        this(titulo, descripcion, identificador);
        if (criterios != null) this.criterios = criterios;
    }

    /**
     * Actualiza los hechos agregando todos los hechos actuales de cada fuente como no consensuados
     */
    public void actualizarHechosIrrestrictos() {
        // Limpiar solo los hechos no consensuados, mantener los consensuados
        hechos.entrySet().removeIf(entry -> !entry.getValue());

        /**for (FuenteDeDatos fuente : fuentes) {
            for (Hecho hecho : fuente.obtenerHechos()) {
                // Agregar como no consensuado si no existe
                hechos.putIfAbsent(hecho, false);
            }
        }*/
        for (FuenteDeDatos fuente : fuentes) {
            for (Hecho hecho : fuente.obtenerHechos()) {
                if (hecho != null) {
                    hechos.putIfAbsent(hecho, false);
                }
            }
        }
    }
    
    /**
     * Actualiza los hechos desde una lista de hechos persistidos de BD
     * Alternativa a actualizarHechosIrrestrictos que evita llamadas REST a fuentes externas
     */
    public void actualizarHechosDesdeListaPersistida(List<Hecho> hechosPersistidos) {
        // Limpiar solo los hechos no consensuados, mantener los consensuados
        hechos.entrySet().removeIf(entry -> !entry.getValue());
        
        // Agregar hechos de BD como no consensuados
        for (Hecho hecho : hechosPersistidos) {
            if (hecho != null) {
                hechos.putIfAbsent(hecho, false);
            }
        }
    }

    /**
     * Ejecuta el algoritmo de consenso para marcar los hechos como consensuados
     */
    public void ejecutarAlgoritmoDeConsenso() {
        List<Hecho> todosLosHechos = new ArrayList<>(hechos.keySet());

        if (algoritmoDeConsenso == null) {
            // Sin algoritmo, todos los hechos se consideran consensuados
            hechos.replaceAll((hecho, consensuado) -> true);
        } else {
            List<List<Hecho>> hechosPorFuente = new ArrayList<>();
            for (FuenteDeDatos fuente : fuentes) {
                hechosPorFuente.add(fuente.obtenerHechos());
            }
            List<Hecho> hechosConsensuadosResultado = algoritmoDeConsenso
                    .filtrarConsensuados(todosLosHechos, hechosPorFuente);

            // Marcar hechos como consensuados según el resultado del algoritmo
            for (Hecho hecho : hechos.keySet()) {
                hechos.put(hecho, hechosConsensuadosResultado.contains(hecho));
            }
        }
    }

    /**
     * Devuelve los hechos según el modo de navegación elegido
     */
    public List<Hecho> obtenerHechosSegunModo(MetodoDeNavegacion modo) {
        if (modo == MetodoDeNavegacion.CURADA) {
            return hechos.entrySet().stream()
                    .filter(Map.Entry::getValue) // Solo hechos consensuados (true)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>(hechos.keySet()); // Todos los hechos
        }
    }

    public List<Hecho> obtenerHechosFiltradosPorCriterios() {
        return hechos.keySet().stream()
                .filter(this::cumpleTodosLosCriterios)
                .collect(Collectors.toList());
    }

    private boolean cumpleTodosLosCriterios(Hecho hecho) {
        return criterios == null || criterios.isEmpty()
                || criterios.stream().allMatch(c -> c.cumpleCriterio(hecho));
    }

    public void agregarHecho(Hecho hecho) {
        if (hecho != null && !hechos.containsKey(hecho)) {
            hechos.put(hecho, false); // Agregar como no consensuado por defecto
        }
    }

    public void agregarHechoConsensuado(Hecho hecho) {
        if (hecho != null) {
            hechos.put(hecho, true); // Agregar o marcar como consensuado
        }
    }

    public List<Hecho> verHechos() {
        return new ArrayList<>(hechos.keySet());
    }

    public List<Hecho> verHechosConsensuados() {
        return hechos.entrySet().stream()
                .filter(Map.Entry::getValue) // Solo hechos consensuados
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public int cantidadHechosConsensuados() {
        return (int) hechos.values().stream().filter(Boolean::booleanValue).count();
    }

    public void removerHecho(Hecho hecho) {
        hechos.remove(hecho);
    }

    public boolean contieneHecho(Hecho hecho) {
        return hechos.containsKey(hecho);
    }

    public List<Hecho> obtenerHechos() {
        return new ArrayList<>(hechos.keySet());
    }

    public List<Hecho> obtenerHechosRecientes() {
        return hechos.keySet().stream().filter(Hecho::esReciente).collect(Collectors.toList());
    }

    public List<Hecho> obtenerHechosConMultimedia() {
        return hechos.keySet().stream().filter(Hecho::tieneContenidoMultimedia).collect(Collectors.toList());
    }

    public List<Hecho> buscarPorTitulo(String titulo) {
        return hechos.keySet().stream()
                .filter(h -> h.getTitulo() != null && h.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Hecho> buscarPorCategoria(String categoria) {
        return hechos.keySet().stream()
                .filter(h -> h.getCategoria() != null && h.getCategoria().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());
    }

    // Métodos adicionales para trabajar con el Map de hechos

    /**
     * Marca un hecho como consensuado
     */
    public void marcarComoConsensuado(Hecho hecho) {
        if (hechos.containsKey(hecho)) {
            hechos.put(hecho, true);
        }
    }

    /**
     * Marca un hecho como no consensuado
     */
    public void marcarComoNoConsensuado(Hecho hecho) {
        if (hechos.containsKey(hecho)) {
            hechos.put(hecho, false);
        }
    }

    /**
     * Verifica si un hecho está consensuado
     */
    public boolean estaConsensuado(Hecho hecho) {
        return hechos.getOrDefault(hecho, false);
    }

    /**
     * Obtiene todos los hechos no consensuados
     */
    public List<Hecho> obtenerHechosNoConsensuados() {
        return hechos.entrySet().stream()
                .filter(entry -> !entry.getValue()) // Solo hechos no consensuados
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}