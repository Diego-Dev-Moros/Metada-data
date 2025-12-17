package com.metamapa.service;

import com.metamapa.dto.ContribuyenteDTO;
import com.metamapa.dto.CrearHechoDTO;
import com.metamapa.dto.EditarHechoDTO;
import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.hechos.Multimedia;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.rol.Rol;
import com.metamapa.entities.mongo.HechoMongo;
import com.metamapa.entities.mongo.ContribuyenteMongo;
import com.metamapa.entities.mongo.UbicacionMongo;
import com.metamapa.repository.mongo.HechoMongoRepository;
import com.metamapa.repository.mongo.ContribuyenteMongoRepository;
import com.metamapa.mapper.MongoEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.metamapa.entities.ubicaciones.Ubicacion;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FuenteDinamicaService implements FuenteDeDatos {
    
    private final HechoMongoRepository hechoRepository;
    private final ContribuyenteMongoRepository contribuyenteRepository;
    private final MongoEntityMapper mapper;
    private final IdGeneratorService idGenerator;

    //private final Map<String, Contribuyente> contribuyentes = new HashMap<>();
    private final String identificador = "fuente-dinamica";
    
    public FuenteDinamicaService(HechoMongoRepository hechoRepository, 
                                ContribuyenteMongoRepository contribuyenteRepository,
                                IdGeneratorService idGenerator) {
        this.hechoRepository = hechoRepository;
        this.contribuyenteRepository = contribuyenteRepository;
        this.mapper = new MongoEntityMapper(); // Crear instancia simple por ahora
        this.idGenerator = idGenerator;
    }
    
    @Override
    public List<Hecho> obtenerHechos() {
        // Obtener hechos aprobados de MongoDB y convertir a entidades JPA
        List<HechoMongo> hechosAprobados = hechoRepository.findHechosAprobados();
        return hechosAprobados.stream()
                .map(mapper::mongoToJpa)
                .collect(Collectors.toList());
    }

    // para obtener todos los hechos sin importar el estado de los mismos
    public List<Hecho> obtenerTodosLosHechos() {
        List<HechoMongo> todosLosHechos = hechoRepository.findAll();
        return todosLosHechos.stream()
                .map(mapper::mongoToJpa)
                .collect(Collectors.toList());
    }

    @Override
    public Hecho obtenerHechoPorId(Long id) {
        if (id == null) {
            return null;
        }
        return hechoRepository.findById(id)
                .map(mapper::mongoToJpa)
                .orElse(null);
    }

    @Override
    public void agregarHecho(Hecho hecho) {
        if (hecho != null && hecho.getTitulo() != null) {
            // Convertir a MongoDB y asignar ID si no tiene
            HechoMongo hechoMongo = mapper.jpaToMongo(hecho);
            if (hechoMongo.getId() == null) {
                hechoMongo.setId(idGenerator.nextHechoId());
            }
            hechoRepository.save(hechoMongo);
            log.info("Hecho agregado a fuente dinámica MongoDB: {}", hecho.getTitulo());
        }
    }
    
    @Override
    public void eliminarHecho(String titulo) {
        // Buscar el hecho por título y eliminarlo
        List<HechoMongo> hechos = hechoRepository.findByTituloContaining(titulo);
        hechos.stream()
            .filter(h -> titulo.equals(h.getTitulo()))
            .findFirst()
            .ifPresent(h -> hechoRepository.deleteById(h.getId()));
        log.info("Hecho eliminado de fuente dinámica MongoDB: {}", titulo);
    }
    
    @Override
    public String getTipo() {
        return "DINAMICA";
    }
    
    @Override
    public String getIdentificador() {
        return identificador;
    }

    // agregar un try catch
    public Hecho crearHecho(/*String titulo, String descripcion, String categoria,
                           double latitud, double longitud, LocalDateTime fechaHecho,
                           String nombreContribuyente, String apellidoContribuyente, 
                           Integer edadContribuyente, List<Multimedia> multimedias,*/CrearHechoDTO dto, Contribuyente contribuyente) {

        //Contribuyente contrib = repository.findContribuyenteById(id)
        //        .orElseThrow(() -> new IllegalArgumentException("Contribuyente no válido"));

        // ESTO ES PORQUE ESTAMOS CREANDO EL CONTRIBUYENTE AL VUELO
        // PERO EL CONTRIBUYENYE YA DEBERIA ESTAR REGISTRADO COMO PARA PODER SUBIR UN HECHO CON AUTENTICACION

        // Crear o obtener contribuyente
        //Contribuyente contribuyente = obtenerOCrearContribuyente(idContribuyente, nombreContribuyente,
        //                                                       apellidoContribuyente,
        //                                                       edadContribuyente);
        
        // Crear ubicación
        //Ubicacion ubicacion = new Ubicacion(latitud, longitud);
        Ubicacion ubicacion = new Ubicacion(dto.getLatitud(), dto.getLongitud());

        // No generar ID manualmente - JPA lo hace automáticamente

        // Convertir fechaHecho de String a LocalDateTime
        LocalDateTime fechaHecho = dto.getFechaHecho() != null && !dto.getFechaHecho().isEmpty() 
            ? LocalDateTime.parse(dto.getFechaHecho()) 
            : LocalDateTime.now();

        // Crear el hecho
        Hecho hecho = new Hecho(dto.getTitulo(), dto.getDescripcion(), dto.getCategoria(), ubicacion, fechaHecho,
                OrigenHecho.CONTRIBUYENTE, null, dto.getUrlsMultimedia());
        // No setear ID manualmente - JPA lo genera automáticamente
        hecho.setContribuyente(contribuyente);
        hecho.cambiarEstadoRevision(EstadoRevision.PENDIENTE);
        hecho.setFechaCarga(LocalDateTime.now());

        // Agregar multimedia si existe
        if (dto.getUrlsMultimedia() != null) {
            dto.getUrlsMultimedia().forEach(hecho::agregarMultimedia);
        }
        
        // Convertir a MongoDB, asignar ID y guardar
        HechoMongo hechoMongo = mapper.jpaToMongo(hecho);
        if (hechoMongo.getId() == null) {
            hechoMongo.setId(idGenerator.nextHechoId());
        }
        HechoMongo savedHecho = hechoRepository.save(hechoMongo);
        
        // Convertir de vuelta a JPA para devolver con ID asignado
        return mapper.mongoToJpa(savedHecho);
    }

    // agregar un try catch
    public Hecho crearHechoAnonimo( /*String titulo, String descripcion, String categoria,
                                  double latitud, double longitud, LocalDateTime fechaHecho,
                                  List<Multimedia> multimedias*/CrearHechoDTO dto, Contribuyente contribuyente) {
        
        // Crear ubicación
        //Ubicacion ubicacion = new Ubicacion(latitud, longitud);
        Ubicacion ubicacion = new Ubicacion(dto.getLatitud(), dto.getLongitud());

        // No generar ID manualmente - JPA lo hace automáticamente

        // Convertir fechaHecho de String a LocalDateTime
        LocalDateTime fechaHecho = dto.getFechaHecho() != null && !dto.getFechaHecho().isEmpty() 
            ? LocalDateTime.parse(dto.getFechaHecho()) 
            : LocalDateTime.now();

        // Crear el hecho sin contribuyente (anónimo)
        Hecho hecho = new Hecho(dto.getTitulo(), dto.getDescripcion(), dto.getCategoria(), ubicacion, fechaHecho,
                               OrigenHecho.CONTRIBUCION_ANONIMA, null, dto.getUrlsMultimedia());
        // No setear ID manualmente - JPA lo genera automáticamente

        hecho.cambiarEstadoRevision(EstadoRevision.PENDIENTE);
        hecho.setFechaCarga(LocalDateTime.now());
        hecho.setEsAnonimo(true);

        if(contribuyente != null) {
            hecho.setContribuyente(contribuyente); // lo guardo (caso hecho anonimo subido por contribuyente)
        }

        // Agregar multimedia si existe
        if (dto.getUrlsMultimedia() != null) {
            dto.getUrlsMultimedia().forEach(hecho::agregarMultimedia);
        }
        
        // Convertir a MongoDB, asignar ID y guardar
        HechoMongo hechoMongo = mapper.jpaToMongo(hecho);
        if (hechoMongo.getId() == null) {
            hechoMongo.setId(idGenerator.nextHechoId());
        }
        HechoMongo savedHecho = hechoRepository.save(hechoMongo);
        
        // Convertir de vuelta a JPA para devolver con ID asignado
        return mapper.mongoToJpa(savedHecho);
    }
    
    public boolean editarHecho(/*Long id, String nuevoTitulo, String nuevaDescripcion,
                              String nuevaCategoria, Double nuevaLatitud, Double nuevaLongitud,
                              LocalDateTime nuevaFechaDelHecho, List<Multimedia> nuevasMultimedias,*/
                                Long id, EditarHechoDTO dto, Contribuyente contribuyente) {
        
        // Verificar si existe el hecho con esa id en MongoDB
        if (id == null) {
            return false;
        }
        HechoMongo hechoMongo = hechoRepository.findById(id).orElse(null);
        if (hechoMongo == null) {
            return false;
        }
        
        // Verificar que el contribuyente sea el autor de dicho hecho
        if (hechoMongo.getContribuyente() == null || 
            !hechoMongo.getContribuyente().getNombre().equals(contribuyente.getNombre())) {
            return false;
        }
        
        // Verificar que no haya pasado una semana
        if (hechoMongo.getFechaCarga().isBefore(LocalDateTime.now().minusWeeks(1))) {
            return false;
        }
        
        // Actualizar el hecho MongoDB
        if (dto.getNuevoTitulo() != null && !dto.getNuevoTitulo().isEmpty()) {
            hechoMongo.setTitulo(dto.getNuevoTitulo());
        }
        if (dto.getNuevaDescripcion() != null && !dto.getNuevaDescripcion().isEmpty()) {
            hechoMongo.setDescripcion(dto.getNuevaDescripcion());
        }
        if (dto.getNuevaCategoria() != null && !dto.getNuevaCategoria().isEmpty()) {
            hechoMongo.setCategoria(dto.getNuevaCategoria());
        }
        if (dto.getNuevaLatitud() != null && hechoMongo.getUbicacion() != null) {
            hechoMongo.getUbicacion().setLatitud(dto.getNuevaLatitud());
        }
        if (dto.getNuevaLongitud() != null && hechoMongo.getUbicacion() != null) {
            hechoMongo.getUbicacion().setLongitud(dto.getNuevaLongitud());
        }
        if (dto.getNuevaFechaDelHecho() != null) {
            hechoMongo.setFechaHecho(dto.getNuevaFechaDelHecho());
        }
        
        // Guardar cambios en MongoDB
        hechoRepository.save(hechoMongo);
        return true;
    }
    
    public boolean revisarHecho(Long id, EstadoRevision estado, String sugerencia, Contribuyente administrador) {
        if (id == null) {
            return false;
        }
        
        HechoMongo hechoMongo = hechoRepository.findById(id).orElse(null);
        
        if(hechoMongo == null) {
            return false;
        }
        
        if(administrador == null || administrador.getRol() != Rol.ADMINISTRADOR) {
            return false;
        }

        hechoMongo.setEstadoRevision(estado);
        if (sugerencia != null && !sugerencia.trim().isEmpty()) {
            hechoMongo.setSugerenciaDeCambio(sugerencia);
        }
        
        // Guardar cambios
        hechoRepository.save(hechoMongo);
        return true;
    }
    
    public List<Hecho> obtenerHechosPendientesRevision() {
        List<HechoMongo> pendientes = hechoRepository.findByEstadoRevision(EstadoRevision.PENDIENTE);
        return pendientes.stream()
                .map(mapper::mongoToJpa)
                .collect(Collectors.toList());
    }

    public List<Hecho> obtenerHechosRechazados() {
        List<HechoMongo> rechazados = hechoRepository.findByEstadoRevision(EstadoRevision.RECHAZADO);
        return rechazados.stream()
                .map(mapper::mongoToJpa)
                .collect(Collectors.toList());
    }

    public List<Hecho> obtenerHechosConSugerencias() {
        List<HechoMongo> conSugerencias = hechoRepository.findByEstadoRevision(EstadoRevision.ACEPTADO_CON_SUGERENCIAS);
        return conSugerencias.stream()
                .map(mapper::mongoToJpa)
                .collect(Collectors.toList());
    }

    // ======================= Contribuyentes ==========================

    // Estamos actualmente cuando carga el hecho estamos registrando a la ves al contribuyente
    /*private Contribuyente obtenerOCrearContribuyente(Long id, String nombre, String apellido, LocalDate fechaNacimiento) {
        if(id != null) {
            return repository.findContribuyenteById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Contribuyente no encontrado con id " + id));
        }
        Contribuyente contribuyente = new Contribuyente();
        contribuyente.setNombre(nombre);
        contribuyente.setApellido(apellido);
        contribuyente.setFechaNacimiento(fechaNacimiento);
        return repository.saveContribuyente(contribuyente);
    }*/
    
    public List<Contribuyente> obtenerContribuyentes() {
        List<ContribuyenteMongo> contribuyentesMongo = contribuyenteRepository.findAll();
        return contribuyentesMongo.stream()
                .map(this::mongoContribuyenteToJpa)
                .collect(Collectors.toList());
    }

    // en lugar de obtener contribuyente podria ser obtener hecho subido por contribuyente
    public Contribuyente obtenerContribuyente(Long id) {
        if (id == null) {
            return null;
        }
        return contribuyenteRepository.findById(id)
                .map(this::mongoContribuyenteToJpa)
                .orElse(null);
    }

    public Contribuyente registrarContribuyente(ContribuyenteDTO contribDto) {
        ContribuyenteMongo contribuyenteMongo = new ContribuyenteMongo();
        contribuyenteMongo.setId(idGenerator.nextContribuyenteId());
        contribuyenteMongo.setNombre(contribDto.getNombre());
        contribuyenteMongo.setApellido(contribDto.getApellido());
        contribuyenteMongo.setFechaNacimiento(contribDto.getFechaNacimiento());
        contribuyenteMongo.setFechaRegistro(LocalDateTime.now());
        contribuyenteMongo.setRol(contribDto.getRol());

        ContribuyenteMongo savedMongo = contribuyenteRepository.save(contribuyenteMongo);
        return mongoContribuyenteToJpa(savedMongo);
    }
    
    // Método auxiliar para convertir ContribuyenteMongo a Contribuyente
    private Contribuyente mongoContribuyenteToJpa(ContribuyenteMongo contrib) {
        if (contrib == null) {
            return null;
        }
        
        Contribuyente contribuyente = new Contribuyente();
        // MongoDB ahora usa Long IDs directamente
        if (contrib.getId() != null) {
            contribuyente.setId(contrib.getId());
        }
        
        contribuyente.setNombre(contrib.getNombre());
        contribuyente.setApellido(contrib.getApellido());
        contribuyente.setFechaNacimiento(contrib.getFechaNacimiento());
        contribuyente.setFechaRegistro(contrib.getFechaRegistro());
        contribuyente.setRol(contrib.getRol());
        
        return contribuyente;
    }
} 