package com.metamapa.service;

import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.HechoOrigenArchivo;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.repository.HechoRepository;
import com.metamapa.repository.HechoOrigenArchivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepuracionService {

    private final HechoRepository hechoRepository;
    private final HechoOrigenArchivoRepository hechoOrigenArchivoRepository;
    private final FingerprintService fingerprintService;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Depura duplicados usando fingerprint.
     * - Mantiene la información del primer hecho.
     * - Incrementa contador y agrega fuente de duplicados.
     */
    @Transactional
    public List<Hecho> depurar(List<Hecho> normalizados) {
        if (normalizados == null || normalizados.isEmpty()) return Collections.emptyList();

        for (Hecho h : normalizados) {
            log.info("📝 Procesando hecho: '{}', esAnonimo={}, contribuyente={}", 
                    h.getTitulo(), h.isEsAnonimo(), h.getContribuyente() != null ? "presente" : "null");
            
            // CRÍTICO: Manejar contribuyente INMEDIATAMENTE antes de cualquier operación con el EntityManager
            if (h.getContribuyente() != null && !h.isEsAnonimo()) {
                Long contribuyenteId = h.getContribuyente().getId();
                log.info("🔍 Contribuyente detectado en hecho, ID: {}", contribuyenteId);
                
                if (contribuyenteId != null) {
                    // Usar getReference() para obtener un proxy managed sin hit a BD
                    // Esto evita el error de "transient instance" porque el proxy ya está managed
                    try {
                        Contribuyente contribuyenteManaged = entityManager.getReference(Contribuyente.class, contribuyenteId);
                        h.setContribuyente(contribuyenteManaged);
                        log.info("✅ Contribuyente managed asignado al hecho - ID: {}", contribuyenteId);
                    } catch (javax.persistence.EntityNotFoundException e) {
                        log.error("❌ Contribuyente con ID {} no encontrado en la base de datos. El hecho será guardado sin contribuyente.", 
                                contribuyenteId);
                        h.setContribuyente(null);
                    }
                } else {
                    log.warn("⚠️ Hecho tiene contribuyente pero sin ID. El hecho será guardado sin contribuyente.");
                    h.setContribuyente(null);
                }
            } else {
                log.info("ℹ️ Hecho sin contribuyente o anónimo, se guardará sin contribuyente");
            }

            String fp = fingerprintService.calcularFingerprint(h);
            h.setFingerprint(fp);

            Optional<Hecho> existente = hechoRepository.findByFingerprint(fp);
            if (existente.isPresent()) {
                Hecho e = existente.get();
                boolean esInformacionNueva = false;

                // Verificar fuentes y agregar solo si son nuevas
                if (h.getFuentes() != null) {
                    for (String f : h.getFuentes()) {
                        // Si la fuente NO estaba en la lista, la agregamos y marcamos novedad
                        if (!e.getFuentes().contains(f)) {
                            e.getFuentes().add(f);
                            esInformacionNueva = true;
                        }
                    }
                }

                // Solo sumamos credibilidad si apareció una fuente DISTINTA
                if (esInformacionNueva) {
                    e.incrementarContador();
                    log.info("📈 Credibilidad aumentada para hecho '{}'. Nueva fuente detectada.", e.getTitulo());
                } else {
                    log.info("🔄 Hecho '{}' ya existente. Misma fuente, no se incrementa contador.", e.getTitulo());
                    // Opcional: Podrías actualizar la fecha de 'ultimaActualizacion' aquí si quisieras
                }

                hechoRepository.save(e);
                
                // CRÍTICO: Guardar relación N-N si este hecho proviene de un archivo
                if (h.getOrigenArchivoId() != null) {
                    guardarRelacionOrigenArchivo(e, h.getOrigenArchivoId());
                }
            } else {
                // Es un hecho totalmente nuevo
                h.setContador(1);
                Hecho hechoGuardado = hechoRepository.save(h);
                
                // CRÍTICO: Guardar relación N-N si este hecho proviene de un archivo
                if (h.getOrigenArchivoId() != null) {
                    guardarRelacionOrigenArchivo(hechoGuardado, h.getOrigenArchivoId());
                }
            }
        }

        // Devuelve todos los hechos actuales en el repositorio (sin duplicados)
        return hechoRepository.findAll();
    }

    /**
     * Guarda la relación N-N entre un hecho y el archivo de origen.
     * Evita duplicados en la tabla intermedia.
     */
    private void guardarRelacionOrigenArchivo(Hecho hecho, Long archivoId) {
        // Verificar si la relación ya existe
        boolean yaExiste = hechoOrigenArchivoRepository.existsByHechoIdAndArchivoId(hecho.getId(), archivoId);
        
        if (!yaExiste) {
            HechoOrigenArchivo relacion = new HechoOrigenArchivo(hecho, archivoId);
            hechoOrigenArchivoRepository.save(relacion);
            log.info("🔗 Relación guardada: Hecho {} ← Archivo {}", hecho.getId(), archivoId);
        } else {
            log.info("ℹ️ Relación Hecho {} ← Archivo {} ya existe, no se duplica", hecho.getId(), archivoId);
        }
    }

    /**
     * Genera fingerprint a partir de título y categoría.
     * Se pueden agregar más campos si es necesario.

    private String buildFingerprint(Hecho h) {
        String titulo = safe(h.getTitulo());
        String categoria = safe(h.getCategoria());
        return titulo + "::" + categoria;
    }*/

    private String safe(String s) { return s == null ? "" : s; }
}
