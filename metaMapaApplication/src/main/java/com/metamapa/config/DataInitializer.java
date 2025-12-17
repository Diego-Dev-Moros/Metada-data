package com.metamapa.config;

import com.metamapa.dto.ContribuyenteDTO;
import com.metamapa.dto.CrearColeccionDTO;
import com.metamapa.entities.colecciones.Absoluta;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.rol.Rol;
import com.metamapa.service.ContribuyenteService;
import com.metamapa.service.ServicioAgregacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final ServicioAgregacion servicioAgregacion;
    private final ContribuyenteService contribuyenteService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Inicializando datos de prueba...");
        
        // Crear un administrador de sistema para las colecciones de ejemplo
        ContribuyenteDTO adminDTO = new ContribuyenteDTO();
        adminDTO.setNombre("Sistema");
        adminDTO.setApellido("Administrador");
        adminDTO.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        adminDTO.setRol(Rol.ADMINISTRADOR);
        
        Contribuyente administrador = contribuyenteService.crearContribuyente(adminDTO);
        log.info("Administrador del sistema creado con ID: {}", administrador.getId());
        
        // Crear colección de ejemplo para Argentina usando el DTO
        CrearColeccionDTO dtoArgentina = new CrearColeccionDTO();
        dtoArgentina.setTitulo("Desastres en Argentina");
        dtoArgentina.setDescripcion("Colección de desastres naturales y tecnológicos en Argentina");
        dtoArgentina.setIdentificador(1L);
        dtoArgentina.setAlgoritmoConsenso("ABSOLUTA");
        
        Coleccion coleccionArgentina = servicioAgregacion.crearColeccion(dtoArgentina, administrador.getId());
        
        // Agregar algunos hechos de ejemplo
        Hecho hecho1 = new Hecho(
                "Incendio forestal en Córdoba",
                "Gran incendio forestal afecta la región de Córdoba",
                "incendio"
        );
        hecho1.setFechaHecho(LocalDateTime.now().minusDays(1));
        
        Hecho hecho2 = new Hecho(
                "Terremoto en San Juan",
                "Sismo de magnitud 6.2 registrado en San Juan",
                "terremoto"
        );
        hecho2.setFechaHecho(LocalDateTime.now().minusDays(5));
        
        Hecho hecho3 = new Hecho(
                "Inundación en Buenos Aires",
                "Fuertes lluvias causan inundaciones en la capital",
                "inundacion"
        );
        hecho3.setFechaHecho(LocalDateTime.now().minusHours(6));
        
        coleccionArgentina.agregarHecho(hecho1);
        coleccionArgentina.agregarHecho(hecho2);
        coleccionArgentina.agregarHecho(hecho3);
        
        // Crear otra colección de ejemplo usando el DTO
        CrearColeccionDTO dtoMundial = new CrearColeccionDTO();
        dtoMundial.setTitulo("Desastres Mundiales");
        dtoMundial.setDescripcion("Colección global de desastres naturales");
        dtoMundial.setIdentificador(2L);
        dtoMundial.setAlgoritmoConsenso("ABSOLUTA");
        
        Coleccion coleccionMundial = servicioAgregacion.crearColeccion(dtoMundial, administrador.getId());
        
        Hecho hechoMundial = new Hecho(
                "Tsunami en Japón",
                "Tsunami afecta las costas de Japón",
                "tsunami"
        );
        hechoMundial.setFechaHecho(LocalDateTime.now().minusDays(3));
        
        coleccionMundial.agregarHecho(hechoMundial);
        
        log.info("Datos de prueba inicializados: {} colecciones creadas", 2);
        log.info("Colección 'Desastres en Argentina' con {} hechos", coleccionArgentina.verHechos().size());
        log.info("Colección 'Desastres Mundiales' con {} hechos", coleccionMundial.verHechos().size());
    }
}
