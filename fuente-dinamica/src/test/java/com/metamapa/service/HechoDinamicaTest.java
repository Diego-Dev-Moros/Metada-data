/*
package com.metamapa.service;

import com.metamapa.domain.ContribuyenteDinamico;
import com.metamapa.domain.HechoDinamico;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.repository.HechoDinamicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HechoDinamicoServiceTest {
    @Mock
    private HechoDinamicoRepository hechoRepository;

    @Mock
    private ContribuyenteDinamicoService contribuyenteService;

    @InjectMocks
    private HechoDinamicoService hechoDinamicoService;

    // BLOQUE 1 --> PRUEBAS DE CREACION (validaciones y valores por defecto)

    @DisplayName("Debe crear un hecho dinamico correctamente estableciendo valores por defecto, para ver que hya" +
            "hay valores que no tenes que setear al crear un hecho dinamicamente")
    @Test
    void crearHechoDinamicamente_CaminoFeliz() {
        HechoDinamico nuevoHecho = new HechoDinamico();
        nuevoHecho.setTitulo("Bache en Avenida Corrientes");
        nuevoHecho.setDescripcion("Hay un bache grande en la esquina.");

        // Mockear el comportamiento del repositorio
        when(hechoRepository.save(any(HechoDinamico.class))).thenAnswer(i -> i.getArguments()[0]);

        HechoDinamico resultado = hechoDinamicoService.crearHecho(nuevoHecho);

        // Verificaciones
        assertNotNull(resultado.getFechaCarga(), "La fecha de carga debe asignarse automaticamente");
        assertEquals(EstadoRevision.PENDIENTE, resultado.getEstadoRevision(), "El estado inical debe ser PENDIENTE");
        assertEquals(OrigenHecho.CONTRIBUYENTE, resultado.getOrigen(), "El origen debe ser CONTRIBUYENTE");
        assertFalse(resultado.isEliminado(), "El hecho no debe nacer como eliminado");

        verify(hechoRepository).save(nuevoHecho); // Verificar que se llamo al repositorio
    }

    @DisplayName("Debe fallar al crear un hecho sin titulo (nulo o vacio")
    @Test
    void crearHecho_FallaSinTitulo() {
        HechoDinamico hechoSinTitulo = new HechoDinamico();
        hechoSinTitulo.setDescripcion("Tengo Descripcion pero NO titulo :(");

        // Verificacion de la excepcion
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            hechoDinamicoService.crearHecho(hechoSinTitulo);
        });

        assertEquals("El título del hecho es requerido", exception.getMessage());
        verify(hechoRepository, never()).save(any()); // Verificamos que NUNCA se llame al repositorio si falla la validación

    }

    // BLOQUE 2 --> PRUEBAS CON CONTRIBUYENTE

    @DisplayName("Debe asociar un contribuyente existente al crear un hecho dinamico")
    @Test
    void crearHechoConContribuyente_CaminoFeliz() {
        String idContribuyente = "user_123";
        ContribuyenteDinamico contribuyenteMock = new ContribuyenteDinamico();
        contribuyenteMock.setId(idContribuyente);

        HechoDinamico hecho = new HechoDinamico();
        hecho.setTitulo("Hecho con Contribuyente");
        hecho.setDescripcion("Descripcion del hecho con contribuyente");

        // Entreno los mocks
        when(contribuyenteService.buscarPorId(idContribuyente)).thenReturn(Optional.of(contribuyenteMock));
        when(hechoRepository.save(any(HechoDinamico.class))).thenAnswer(i -> i.getArgument(0));

        HechoDinamico resultado = hechoDinamicoService.crearHechoConContribuyente(hecho, idContribuyente);

        // Verificaciones
        assertNotNull(resultado.getContribuyente());
        assertEquals(idContribuyente, resultado.getContribuyente().getId());
        assertFalse(resultado.isEsAnonimo(), "Si tiene contribuyente, no debe ser anonimo por defecto");
    }

    @DisplayName("Debe fallar si el contribuyente NO existe")
    @Test
    void crearHechoConContribuyente_FallaSiNoExisteContribuyente() {
        String idContribuyente = "user_no_existe";

        HechoDinamico hecho = new HechoDinamico();
        hecho.setTitulo("Hecho con Contribuyente Inexistente");
        hecho.setDescripcion("Descripcion del hecho");

        when(contribuyenteService.buscarPorId(idContribuyente)).thenReturn(Optional.empty());

        // Verificacion de la excepcion
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            hechoDinamicoService.crearHechoConContribuyente(hecho, idContribuyente);
        });

        assertEquals("No se encontró el contribuyente especificado", exception.getMessage());
        verify(hechoRepository, never()).save(any()); // Verificamos que NUNCA se llame al repositorio si falla la validación
    }

    // BLOQUE 3 --> PRUEBAS DE ELIMINACION (QUE NO SE ELIMINA EN REALIDAD)

    @DisplayName("Debe marcar como eliminado en lugar de borrar la fsicamente")
    @Test
    void eliminarHecho_MarcarComoEliminado() {
        String idHecho = "hecho_123";
        HechoDinamico hechoExistente = new HechoDinamico();
        hechoExistente.setId(idHecho);
        hechoExistente.setEliminado(false); // Empieza activo!!

        // Simular la busqueda del hecho existente
        when(hechoRepository.findById(idHecho)).thenReturn(Optional.of(hechoExistente));

        hechoDinamicoService.eliminarHecho(idHecho);

        // Verificaciones
        assertTrue(hechoExistente.isEliminado(), "El hecho no se puede eliminar");
        verify(hechoRepository).save(hechoExistente);   // Verificar que se guardo el cambio
        verify(hechoRepository, never()).delete(any()); // Verificar que NUNCA se llame a delete (NO BORRO FISICAMENTE)
    }
}
*/