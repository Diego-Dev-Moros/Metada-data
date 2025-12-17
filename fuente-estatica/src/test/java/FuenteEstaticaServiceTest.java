/*
package com.metamapa.service;

import com.metamapa.dto.CargaCsvResultado;
import com.metamapa.entities.mongo.ArchivoDatasetMongo;
import com.metamapa.entities.mongo.HechoEstatico;
import com.metamapa.repository.mongo.ArchivoDatasetMongoRepository;
import com.metamapa.repository.mongo.HechoEstaticoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuenteEstaticaServiceTest {
    @Mock
    private HechoEstaticoRepository hechoRepository;

    @Mock
    private ArchivoDatasetMongoRepository archivoRepository;

    @InjectMocks
    private FuenteEstaticaService fuenteEstaticaService;

    // TEST 1 --> CARGA EXITOSA DE CSV

    @DisplayName("Debe cargar un archivo CSV correctamente y guardar los hechos en la base de datos")
    @Test
    void cargarHechos_Exitoso() {
        // Tengo que crear un contenido CSV simulado
        String contenidoCsv = "Titulo,Desc,Cat,Lat,Lon,Fecha\n" +
                "Incendio 1,Fuego en bosque,Incendio,-34.6,-58.4,2025-01-01 12:00:00"; // solo un hecho tiene dentro

        // Simula la subida de un archivo CSV
        MockMultipartFile file = new MockMultipartFile(
                "data", "test.csv", "text/csv", contenidoCsv.getBytes());

        // Mockeo los repositorios
        when(hechoRepository.findByTituloIgnoreCase(anyString())).thenReturn(Optional.empty());
        // Simular guardados
        when(archivoRepository.save(any(ArchivoDatasetMongo.class))).thenAnswer(i -> i.getArgument(0));

        CargaCsvResultado resultado = fuenteEstaticaService.cargarHechos(file);

        // Verificaciones
        assertEquals(1, resultado.getProcesadas(), "Debe procesar un hecho del CSV");
        assertEquals(1, resultado.getInsertadas(), "Debe insertar un hecho nuevo");
        assertEquals(0, resultado.getReemplazadas());
        assertTrue(resultado.getErrores().isEmpty());

        // Verifico que se guardo el hecho con los datos correctos
        verify(hechoRepository, times(1)).save(argThat(hecho ->
                hecho.getTitulo().equals("Incendio 1") &&
                        hecho.getUbicacion().getLatitud() == -34.6
        ));
    }

    // TEST 2 --> LOGICA DE REEMPLAZO

    @DisplayName("Debe reemplazar (borrar y crear) si el hecho ya existe por título")
    @Test
    void cargarHechos_HechoExistente_Reemplazo(){
        // CSV con un hecho con un titulo que ya existe
        String contenidoCsv = "Header\n" +
                "Inundación La Plata,Agua subiendo,Inundación,-34.9,-57.9,02/04/2025 10:00:00";

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", contenidoCsv.getBytes());

        // Simulo que ya existe en la base de datos un hecho con ese titulo
        HechoEstatico existente = new HechoEstatico();
        existente.setTitulo("Inundación La Plata");

        when(hechoRepository.findByTituloIgnoreCase("Inundación La Plata"))
                .thenReturn(Optional.of(existente));

        CargaCsvResultado resultado = fuenteEstaticaService.cargarHechos(file);

        // Verificaciones
        assertEquals(1, resultado.getProcesadas(), "Debe procesar un hecho del CSV");
        assertEquals(0, resultado.getInsertadas(), "No debe insertar, ya que existe");
        assertEquals(1, resultado.getReemplazadas(), "Debe contar como reemplazo");

        // Verifico que se haya llamado a delete y luego a save
        verify(hechoRepository).deleteByTituloIgnoreCase("Inundación La Plata");
        verify(hechoRepository).save(any(HechoEstatico.class));
    }

    // TEST 3 --> VALIDACIONES Y ERRORES EN LOS FORMATOS DE LOS PARAMETROS PASADOS

    @DisplayName("Debe manejar errores de formato (fechas mal, columnas faltantes) y reportarlos")
    @Test
    void cargarHechos_ConErrores_ReportaCorrectamente() {
        String contenidoCsv = "H1,H2,H3,H4,H5,H6\n" +
                "Caso Valido,Desc,Cat,10,10,2025-01-01 10:00:00\n" + // OK
                "Caso Fecha Mal,Desc,Cat,10,10,FECHA_INVALIDA\n" +   // Error Fecha
                "Caso Columnas,,,,\n" +                              // Error Columnas (<6)
                "Caso Latitud,Desc,Cat,INVALID,10,2025-01-01";       // Error Formato numero

        MockMultipartFile file = new MockMultipartFile("file", contenidoCsv.getBytes());

        // Mockeo repositorio para que no haya hechos previos
        when(hechoRepository.findByTituloIgnoreCase(anyString())).thenReturn(Optional.empty());

        CargaCsvResultado resultado = fuenteEstaticaService.cargarHechos(file);

        // Verificar contadores
        // 1 válida procesada
        // 3 fallidas (Fecha, Columnas, Latitud)

        // Justificacion de porque fallan....
        // Fila 1 (Valido): Pasa Loop 1, Pasa Loop 2 -> Insertado.
        // Fila 2 (Fecha Mal): Pasa Loop 1, Falla Loop 2 -> Salteada.
        // Fila 3 (Columnas): Falla Loop 1 -> Salteada.
        // Fila 4 (Lat Mal): Pasa Loop 1, Falla Loop 2 -> Salteada.

        assertEquals(1, resultado.getInsertadas());
        assertEquals(3, resultado.getSalteadas());
        assertEquals(3, resultado.getErrores().size());

        assertTrue(resultado.getErrores().get(0).contains("fecha inválida"));
        assertTrue(resultado.getErrores().get(1).contains("columnas insuficientes"));
        assertTrue(resultado.getErrores().get(2).contains("lat/long inválidas"));

    }

    // TEST 4 --> PARSEO DE FLEXIBILIDAD DE FECHAS

    @DisplayName("Debe soportar múltiples formatos de fecha")
    @Test
    void cargarHechos_PruebaFormatosFecha() {
        // Probamos los difentes formatos definidos
        String contenidoCsv = "H1,H2,H3,H4,H5,H6\n" +
                "F1,D,C,0,0,2025-12-31 23:59:59\n" +  // ISO
                "F2,D,C,0,0,31/12/2025 23:59:59\n" +  // DD/MM/YYYY
                "F3,D,C,0,0,12/31/2025 23:59:59\n" +  // MM/DD/YYYY
                "F4,D,C,0,0,2025-12-31";              // Solo Fecha (sin tiempo)

        MockMultipartFile file = new MockMultipartFile("file", contenidoCsv.getBytes());

        fuenteEstaticaService.cargarHechos(file);

        // Verificamos que se llamó a save 4 veces (significa que parseó las 4 fechas bien)
        verify(hechoRepository, times(4)).save(any());
    }
}
*/