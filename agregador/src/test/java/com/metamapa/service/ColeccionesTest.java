/*
package com.metamapa.service;

import com.metamapa.dto.CrearColeccionDTO;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.repository.ColeccionRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PruebaInicialTest {

    @Test
    void pruebaDeEjemplo() {
        String hola = "Hola, Mundo!";
        assertEquals("Hola, Mundo!", hola);
    }
}

@ExtendWith(MockitoExtension.class) // --> Esto me habilita el uso de @Mock y @InjectMocks (Mockito)
class ColeccionTest {

    // 2. Definimos los Mocks (las dependencias falsas)
    @Mock
    private ColeccionRepository coleccionRepository;
    @Mock
    private ContribuyenteService contribuyenteService;
    @Mock
    private IdGeneratorService idGeneratorService;
    @Mock
    private CriterioService criterioService;

    // Agregar otros repositorios o servicios necesarios como mocks

    @Mock
    private com.metamapa.repository.HechoRepository hechoRepository;

    // 3. Inyectamos los mocks dentro del servicio real
    @InjectMocks
    private ServicioAgregacion servicioAgregacion;

    @Test
    public void TestCrearColeccionConDTO() {

        // Preparacion de los datos
        CrearColeccionDTO dto = new CrearColeccionDTO();
        dto.setTitulo("Colección de Prueba");
        dto.setDescripcion("Esta es una descripción de prueba para el test");
        dto.setAlgoritmoConsenso("Mayoria_Simple"); // Debe coincidir con tu switch en el servicio
        dto.setCriterios(new ArrayList<>()); // Lista vacía para evitar nulos
        dto.setFuenteIds(new ArrayList<>());

        // Hacemos un Administrador falso (Mock)
        Long idAdmin = 1L;
        Contribuyente adminMock = new Contribuyente();
        adminMock.setId(idAdmin);
        adminMock.setNombre("Nombre Test");
        adminMock.setApellido("Apellido Test");
        adminMock.setFechaNacimiento("1990-01-01");

        // Entreno a los mocks

        // Cuando el servicio pida un ID para la colección... le doy 100L
        when(idGeneratorService.generarIdColeccion()).thenReturn(100L);

        // Cuando el servicio busque al administrador... devuelve nuestro admin falso
        when(contribuyenteService.obtenerContribuyentePorId(idAdmin)).thenReturn(adminMock);

        // Cuando el servicio intente guardar en BD... devuelve la misma colección que le pasaron
        when(coleccionRepository.save(any(Coleccion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecuto el servicio a testear
        Coleccion resultado = servicioAgregacion.crearColeccionConDTO(dto, idAdmin);

        // Verificaciones
        assertNotNull(resultado, "La coleecion no deberia ser nula");
        assertEquals(100L, resultado.getIdentificador(), "El ID de la coleccion no es el esperado");
        assertEquals("Colección de Prueba", resultado.getTitulo(), "El titulo de la coleccion no es el esperado");
        assertEquals("Esta es una descripción de prueba para el test", resultado.getDescripcion(), "La descripcion de la coleccion no es la esperada");
        assertEquals(adminMock, resultado.getAdministrador(), "El administrador de la coleccion no es el esperado");

        System.out.println("Test Exitoso: Se creó la colección " + resultado.getTitulo());
    }

    @DisplayName("Filtrar solo hechos con categoria Incendio")
    @Test
    void filtrarHechos_soloHechosDeIncendio() {
        Long idAdmin = 1L;
        Long idColeccion = 99L;

        when(contribuyenteService.obtenerContribuyentePorId(idAdmin)).thenReturn(new Contribuyente());
        when(idGeneratorService.generarIdColeccion()).thenReturn(idColeccion);
        when(coleccionRepository.save(any(Coleccion.class))).thenAnswer(i -> i.getArgument(0));

        CrearColeccionDTO dto = new  CrearColeccionDTO();
        dto.setTitulo("Coleccion mixta de hechos");
        dto.setDescripcion("Descripcion de prueba");
        dto.setAlgoritmoConsenso("por_defecto");

        // Agregramos hechos a la coleccion de forma manual

        // SEGUIR PERO ANTES ME FUI CON OTRA COSA PORQUE NO TENGO LOS CONTROLLERS A MANO

    }
}
*/