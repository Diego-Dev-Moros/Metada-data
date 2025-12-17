/*
package com.metamapa.service;

import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.solicitudes.EstadoSolicitud;
import com.metamapa.entities.solicitudes.SolicitudEliminacion;
import com.metamapa.repository.HechoRepository;
import com.metamapa.repository.SolicitudEliminacionRepository;
import com.metamapa.spam.DetectorDeSpam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudEliminacionTest {
    @Mock
    private SolicitudEliminacionRepository solicitudRepository;
    @Mock
    private HechoRepository hechoRepository;
    @Mock
    private DetectorDeSpam detectorDeSpam;
    @Mock
    private ServicioAgregacion servicioAgregacion;
    @Mock
    private EntityManager entityManager; // Necesario porque tu código hace un .flush()

    @InjectMocks
    private SolicitudService solicitudService;

    // TESTS 1 : CREACION DE SOLICITUDES DE ELIMINACION

    @DisplayName("Debe crear una solicitud de eliminación correctamente en PENDIENTE cuando la justificación es válida y no es spam")
    @Test
    void crearSolicitudEliminacion_Valida_NoSpam() {
        Hecho hechoMock = new Hecho();
        hechoMock.setTitulo("Hecho Valido");
        String justificacion = "Esta es una justificación válida que tiene más de cincuenta caracteres para pasar la validación.";

        // Simulamos que NO es Spam
        when(detectorDeSpam.esSpam(justificacion)).thenReturn(false);
        // Simulamos el guardado
        when(solicitudRepository.save(any(SolicitudEliminacion.class))).thenAnswer(i -> i.getArgument(0));

        SolicitudEliminacion resultado = solicitudService.crearSolicitudEliminacion(hechoMock, justificacion, new Contribuyente());

        // Validaciones
        assertEquals(EstadoSolicitud.PENDIENTE, resultado.getEstado(), "Debe iniciar en estado PENDIENTE");
        assertFalse(resultado.getEsSpam(), "No debe ser marcado como spam");
        assertNotNull(resultado.getFechaSolicitud(), "Debe tener fecha de solicitud asignada");
        verify(solicitudRepository).save(any()); // Verificamos que se llamó al repositorio
    }

    @DisplayName("Debe marcar como RECHAZADA automaticamente si es Spam")
    @Test
    void crearSoliticitud_EsSpam() {
        Hecho hechoMock = new Hecho();
        String textoSpam = "Compre esto barato, tiene más de 50 caracteres pero es spam puro.";

        // Simulamos que SI es Spam
        when(detectorDeSpam.esSpam(textoSpam)).thenReturn(true);
        when(solicitudRepository.save(any(SolicitudEliminacion.class))).thenAnswer(i -> i.getArgument(0));

        SolicitudEliminacion resultado = solicitudService.crearSolicitudEliminacion(hechoMock, textoSpam, null);

        // Validaciones
        assertEquals(EstadoSolicitud.RECHAZADA, resultado.getEstado(), "Debe iniciar en estado RECHAZADA por ser spam");
        assertTrue(resultado.getEsSpam(), "Debe ser marcado como spam");
    }

    @DisplayName("Debe fallar al crear una solicitud con justificación menor a 50 caracteres")
    @Test
    void crearSolicitudEliminacion_JustificacionCorta_Falla() {
        Hecho hechoMock = new Hecho();
        String justificacionCorta = "Muy corta."; // < 50 caracteres

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            solicitudService.crearSolicitudEliminacion(hechoMock, justificacionCorta, null);
        });

        assertTrue(ex.getMessage().contains("50 caracteres"), "Debe mencionar el error de longitud");
        verifyNoInteractions(solicitudRepository); // No debe intentar guardar nada
    }

    // TESTS 2 : APROBACION DE SOLICITUDES

    @DisplayName("Aprobar solicitud: Debe cambiar estado, eliminar el hecho y actualizar colecciones")
    @Test
    void aprobarSolitud_CaminoFeliz() {
        Long idSolicitud = 100L;

        Hecho hechoReal = new Hecho();
        hechoReal.setId(50L);
        hechoReal.setEliminado(false);

        SolicitudEliminacion solicitudExistente = new SolicitudEliminacion();
        solicitudExistente.setId(idSolicitud);
        solicitudExistente.setEstado(EstadoSolicitud.PENDIENTE);
        solicitudExistente.setHecho(hechoReal);

        // Simulamos la búsqueda de la solicitud y que la encontramos
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Optional.of(solicitudExistente));

        // Simulamos guardados
        when(hechoRepository.save(any(Hecho.class))).thenAnswer(i -> i.getArgument(0));
        when(solicitudRepository.save(any(SolicitudEliminacion.class))).thenAnswer(i -> i.getArgument(0));

        solicitudService.aprobarSolicitud(idSolicitud);

        // Validaciones
        assertEquals(EstadoSoliticitud.ACEPTADA, solicitudExistente.getEstado(), "La solicitud debe quedar como ACEPTADA");
        assertNotNull(solicitudExistente.getFechaResolucion(), "Debe tener fecha de resolución");

        assertTrue(hechoReal.isEliminado(), "El hecho vinculado debe marcarse como eliminado");
        verify(hechoRepository).save(hechoReal);

        verify(entityManager).flush(); // llamaMOS explícitamente a flush
        verify(servicioAgregacion).actualizarTodasLasColecciones(); // Debe avisar al agregador

    }

    @DisplayName("Aprobar solicitud: No debe hacer nada si el ID no existe")
    @Test
    void aprobarSolicitud_IdInexistente() {
        Long idFalso = 999L;
        when(solicitudRepository.findById(idFalso)).thenReturn(Optional.empty());

        solicitudService.aprobarSolicitud(idFalso);

        // Verificaciones
        verify(hechoRepository, never()).save(any());
        verify(servicioAgregacion, never()).actualizarTodasLasColecciones();
    }

    // TESTS 3 : RECHAZO DE SOLICITUDES

    @DisplayName("Rechazar solicitud: Solo debe cambiar el estado")
    @Test
    void rechazarSolicitud_CaminoFeliz() {
        Long idSolicitud = 200L;
        SolicitudEliminacion solicitud = new SolicitudEliminacion();
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        when(solicitudRepository.findById(idSolicitud)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        solicitudService.rechazarSolicitud(idSolicitud);

        // Validaciones
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado(), "El estado debe cambiar a RECHAZADA");
        assertNotNull(solicitud.getFechaResolucion(), "Debe tener fecha de resolución");

        verifyNoInteractions(hechoRepository); // No se toca el hecho asociado
        verifyNoInteractions(servicioAgregacion); // No se actualizan las colecciones
    }

}
*/