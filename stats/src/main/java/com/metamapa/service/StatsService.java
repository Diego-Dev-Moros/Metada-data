package com.metamapa.service;

import com.metamapa.dto.*;
import com.metamapa.entity.StatsMetadata;
import com.metamapa.entity.StatsProvincia;
import com.metamapa.entity.StatsCategoria;
import com.metamapa.entity.StatsHora;
import com.metamapa.entity.StatsSpam;
import com.metamapa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Servicio principal de estadísticas
 * Coordina el cálculo y persistencia de todas las estadísticas
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StatsService {

    private final HechoRepository hechoRepository;
    private final com.metamapa.repository.ColeccionHechoRepository coleccionHechoRepository;
    private final SolicitudEliminacionRepository solicitudEliminacionRepository;
    private final StatsProvinciaRepository statsProvinciaRepository;
    private final StatsCategoriaRepository statsCategoriaRepository;
    private final StatsHoraRepository statsHoraRepository;
    private final StatsSpamRepository statsSpamRepository;
    private final StatsMetadataRepository statsMetadataRepository;

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    /**
     * Recalcular todas las estadísticas
     */
    public void recalcularEstadisticas() {
        log.info("Iniciando recálculo de estadísticas...");

        try {
            // 1. Calcular estadísticas de provincias
            List<StatsProvincia> statsProvincias = calcularStatsProvincias();

            // 2. Calcular estadísticas de categorías
            List<StatsCategoria> statsCategorias = calcularStatsCategorias();

            // 3. Calcular estadísticas de horas
            List<StatsHora> statsHoras = calcularStatsHoras();

            // 4. Calcular estadísticas de spam
            List<StatsSpam> statsSpam = calcularStatsSpam();

            // 5. Persistir todas las estadísticas
            persistirEstadisticas(statsProvincias, statsCategorias, statsHoras, statsSpam);

            // 6. Actualizar metadatos
            actualizarMetadatos();

            // 7. Limpiar caché
            cache.clear();

            log.info("Estadísticas recalculadas y persistidas exitosamente");

        } catch (Exception e) {
            log.error("Error al recalcular estadísticas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al recalcular estadísticas", e);
        }
    }

    /**
     * Obtener provincia con más hechos por colección
     */
    public ProvinciaStatsDTO getProvinciaConMasHechos(Long coleccionId) {
        String cacheKey = "provincia-" + coleccionId;

        // 1. Verificar caché
        if (cache.containsKey(cacheKey)) {
            log.info("Estadísticas obtenidas del caché para colección {}", coleccionId);
            return (ProvinciaStatsDTO) cache.get(cacheKey);
        }

        // 2. Buscar en BD
        Optional<StatsProvincia> stats = statsProvinciaRepository
                .findTopByColeccionIdOrderByCantidadHechosDesc(coleccionId);

        if (stats.isPresent()) {
            ProvinciaStatsDTO dto = convertirProvinciaADTO(stats.get());
            cache.put(cacheKey, dto);
            return dto;
        }

        // 3. Si no existe, calcular desde BD del agregador
        return calcularProvinciaDesdeBD(coleccionId);
    }

    /**
     * Obtener categoría con más hechos
     */
    public CategoriaStatsDTO getCategoriaConMasHechos() {
        String cacheKey = "categoria-mas-hechos";

        // 1. Verificar caché
        if (cache.containsKey(cacheKey)) {
            log.info("Estadísticas obtenidas del caché para categoría");
            return (CategoriaStatsDTO) cache.get(cacheKey);
        }

        // 2. Buscar en BD
        Optional<StatsCategoria> stats = statsCategoriaRepository.findTopOrderByCantidadHechosDesc();

        if (stats.isPresent()) {
            CategoriaStatsDTO dto = convertirCategoriaADTO(stats.get());
            cache.put(cacheKey, dto);
            return dto;
        }

        // 3. Si no existe, calcular desde BD del agregador
        return calcularCategoriaDesdeBD();
    }

    /**
     * Obtener provincia con más hechos de una categoría específica
     */
    public ProvinciaStatsDTO getProvinciaPorCategoria(String categoria) {
        String cacheKey = "provincia-categoria-" + categoria;

        // 1. Verificar caché
        if (cache.containsKey(cacheKey)) {
            log.info("Estadísticas obtenidas del caché para provincia-categoría {}", categoria);
            return (ProvinciaStatsDTO) cache.get(cacheKey);
        }

        // 2. Calcular desde BD del agregador
        return calcularProvinciaPorCategoriaDesdeBD(categoria);
    }

    /**
     * Obtener hora con más hechos de una categoría específica
     */
    public HoraStatsDTO getHoraPorCategoria(String categoria) {
        String cacheKey = "hora-categoria-" + categoria;

        // 1. Verificar caché
        if (cache.containsKey(cacheKey)) {
            log.info("Estadísticas obtenidas del caché para hora-categoría {}", categoria);
            return (HoraStatsDTO) cache.get(cacheKey);
        }

        // 2. Calcular desde BD del agregador
        return calcularHoraPorCategoriaDesdeBD(categoria);
    }

    /**
     * Obtener estadísticas de spam
     */
    public SpamStatsDTO getSpamEliminaciones() {
        String cacheKey = "spam-eliminaciones";

        // 1. Verificar caché
        if (cache.containsKey(cacheKey)) {
            log.info("Estadísticas obtenidas del caché para spam");
            return (SpamStatsDTO) cache.get(cacheKey);
        }

        // 2. Buscar en BD
        Optional<StatsSpam> stats = statsSpamRepository.findLatest();

        if (stats.isPresent()) {
            SpamStatsDTO dto = convertirSpamADTO(stats.get());
            cache.put(cacheKey, dto);
            return dto;
        }

        // 3. Si no existe, calcular desde BD del agregador
        return calcularSpamDesdeBD();
    }

    // Métodos privados para cálculos específicos
    private List<StatsProvincia> calcularStatsProvincias() {
        log.info("Calculando estadísticas por provincia...");
        List<StatsProvincia> statsProvincias = new ArrayList<>();

        try {
            // Obtener todas las colecciones únicas desde la tabla de relación ColeccionHecho
            List<Long> colecciones = coleccionHechoRepository.findDistinctColeccionIds();

            for (Long coleccionId : colecciones) {
                List<Object[]> resultados = hechoRepository.findStatsPorProvincia(coleccionId);

                if (!resultados.isEmpty()) {
                    Long totalHechosColeccion = resultados.stream()
                            .mapToLong(resultado -> (Long) resultado[1])
                            .sum();

                    for (Object[] resultado : resultados) {
                        String provincia = (String) resultado[0];
                        Long cantidadHechos = (Long) resultado[1];
                        Double porcentaje = totalHechosColeccion > 0 ?
                                (cantidadHechos * 100.0 / totalHechosColeccion) : 0.0;

                        StatsProvincia stats = new StatsProvincia(
                                coleccionId, provincia, cantidadHechos, LocalDateTime.now()
                        );
                        stats.setPorcentaje(porcentaje);
                        statsProvincias.add(stats);
                    }
                }
            }

            log.info("Calculadas {} estadísticas por provincia", statsProvincias.size());

        } catch (Exception e) {
            log.error("Error al calcular estadísticas por provincia: {}", e.getMessage());
        }

        return statsProvincias;
    }

    private List<StatsCategoria> calcularStatsCategorias() {
        log.info("Calculando estadísticas por categoría...");
        List<StatsCategoria> statsCategorias = new ArrayList<>();

        try {
            List<Object[]> resultados = hechoRepository.findStatsPorCategoria();

            if (!resultados.isEmpty()) {
                Long totalHechos = resultados.stream()
                        .mapToLong(resultado -> (Long) resultado[1])
                        .sum();

                for (Object[] resultado : resultados) {
                    String categoria = (String) resultado[0];
                    Long cantidadHechos = (Long) resultado[1];
                    Double porcentaje = totalHechos > 0 ?
                            (cantidadHechos * 100.0 / totalHechos) : 0.0;

                    StatsCategoria stats = new StatsCategoria(
                            categoria, cantidadHechos, LocalDateTime.now()
                    );
                    stats.setPorcentaje(porcentaje);
                    statsCategorias.add(stats);
                }
            }

            log.info("Calculadas {} estadísticas por categoría", statsCategorias.size());

        } catch (Exception e) {
            log.error("Error al calcular estadísticas por categoría: {}", e.getMessage());
        }

        return statsCategorias;
    }

    private List<StatsHora> calcularStatsHoras() {
        log.info("Calculando estadísticas por hora...");
        List<StatsHora> statsHoras = new ArrayList<>();

        try {
            // Obtener todas las categorías únicas
            List<String> categorias = hechoRepository.findAll().stream()
                    .map(h -> h.getCategoria())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            for (String categoria : categorias) {
                List<Object[]> resultados = hechoRepository.findStatsPorHoraYCategoria(categoria);

                if (!resultados.isEmpty()) {
                    Long totalHechosCategoria = resultados.stream()
                            .mapToLong(resultado -> (Long) resultado[1])
                            .sum();

                    for (Object[] resultado : resultados) {
                        Integer hora = (Integer) resultado[0];
                        Long cantidadHechos = (Long) resultado[1];
                        Double porcentaje = totalHechosCategoria > 0 ?
                                (cantidadHechos * 100.0 / totalHechosCategoria) : 0.0;

                        StatsHora stats = new StatsHora(
                                categoria, hora, cantidadHechos, LocalDateTime.now()
                        );
                        stats.setPorcentaje(porcentaje);
                        statsHoras.add(stats);
                    }
                }
            }

            log.info("Calculadas {} estadísticas por hora", statsHoras.size());

        } catch (Exception e) {
            log.error("Error al calcular estadísticas por hora: {}", e.getMessage());
        }

        return statsHoras;
    }

    private List<StatsSpam> calcularStatsSpam() {
        log.info("Calculando estadísticas de spam...");
        List<StatsSpam> statsSpam = new ArrayList<>();

        try {
            Long totalSolicitudes = solicitudEliminacionRepository.countTotalSolicitudes();
            Long solicitudesSpam = solicitudEliminacionRepository.countSolicitudesSpam();
            Double porcentajeSpam = solicitudEliminacionRepository.calcularPorcentajeSpam();

            if (totalSolicitudes != null && totalSolicitudes > 0) {
                StatsSpam stats = new StatsSpam(
                        totalSolicitudes,
                        solicitudesSpam,
                        porcentajeSpam != null ? porcentajeSpam : 0.0,
                        LocalDateTime.now()
                );
                statsSpam.add(stats);
            }

            log.info("Calculadas {} estadísticas de spam", statsSpam.size());

        } catch (Exception e) {
            log.error("Error al calcular estadísticas de spam: {}", e.getMessage());
        }

        return statsSpam;
    }

    private void persistirEstadisticas(List<StatsProvincia> provincias,
                                       List<StatsCategoria> categorias,
                                       List<StatsHora> horas,
                                       List<StatsSpam> spam) {
        // Limpiar estadísticas anteriores
        statsProvinciaRepository.deleteAll();
        statsCategoriaRepository.deleteAll();
        statsHoraRepository.deleteAll();
        statsSpamRepository.deleteAll();

        // Persistir nuevas estadísticas
        statsProvinciaRepository.saveAll(provincias);
        statsCategoriaRepository.saveAll(categorias);
        statsHoraRepository.saveAll(horas);
        statsSpamRepository.saveAll(spam);

        log.info("Estadísticas persistidas: {} provincias, {} categorías, {} horas, {} spam",
                provincias.size(), categorias.size(), horas.size(), spam.size());
    }

    private void actualizarMetadatos() {
        StatsMetadata metadata = statsMetadataRepository.findStatsMetadata()
                .orElse(new StatsMetadata(LocalDateTime.now(), 0L, "1.0"));

        metadata.setUltimaActualizacionStats(LocalDateTime.now());
        metadata.setTotalHechosProcesados(hechoRepository.countTotalHechos());

        statsMetadataRepository.save(metadata);
    }

    // Métodos de conversión DTO
    private ProvinciaStatsDTO convertirProvinciaADTO(StatsProvincia stats) {
        return ProvinciaStatsDTO.builder()
                .provincia(stats.getProvincia())
                .cantidadHechos(stats.getCantidadHechos())
                .porcentaje(stats.getPorcentaje())
                .fechaCalculo(stats.getFechaCalculo())
                .coleccionId(stats.getColeccionId())
                .build();
    }

    private CategoriaStatsDTO convertirCategoriaADTO(StatsCategoria stats) {
        return CategoriaStatsDTO.builder()
                .categoria(stats.getCategoria())
                .cantidadHechos(stats.getCantidadHechos())
                .porcentaje(stats.getPorcentaje())
                .fechaCalculo(stats.getFechaCalculo())
                .build();
    }

    private SpamStatsDTO convertirSpamADTO(StatsSpam stats) {
        return SpamStatsDTO.builder()
                .totalSolicitudes(stats.getTotalSolicitudes())
                .solicitudesSpam(stats.getSolicitudesSpam())
                .porcentajeSpam(stats.getPorcentajeSpam())
                .fechaCalculo(stats.getFechaCalculo())
                .build();
    }

    // Métodos de cálculo desde BD (implementar después)
    private ProvinciaStatsDTO calcularProvinciaDesdeBD(Long coleccionId) {
        log.info("Calculando provincia con más hechos para colección: {}", coleccionId);

        // 1. Obtener estadísticas por provincia desde BD
        List<Object[]> resultados = hechoRepository.findStatsPorProvincia(coleccionId);

        if (resultados.isEmpty()) {
            log.warn("No se encontraron hechos para la colección: {}", coleccionId);
            return ProvinciaStatsDTO.builder()
                    .provincia("Sin datos")
                    .cantidadHechos(0L)
                    .porcentaje(0.0)
                    .fechaCalculo(LocalDateTime.now())
                    .coleccionId(coleccionId)
                    .build();
        }

        // 2. Obtener el primer resultado (el que tiene más hechos)
        Object[] primerResultado = resultados.get(0);
        String provincia = (String) primerResultado[0];
        Long cantidadHechos = (Long) primerResultado[1];

        // 3. Calcular porcentaje
        Long totalHechosColeccion = resultados.stream()
                .mapToLong(resultado -> (Long) resultado[1])
                .sum();

        Double porcentaje = totalHechosColeccion > 0 ?
                (cantidadHechos * 100.0 / totalHechosColeccion) : 0.0;

        // 4. Crear y retornar DTO
        ProvinciaStatsDTO dto = ProvinciaStatsDTO.builder()
                .provincia(provincia)
                .cantidadHechos(cantidadHechos)
                .porcentaje(porcentaje)
                .fechaCalculo(LocalDateTime.now())
                .coleccionId(coleccionId)
                .build();

        log.info("Provincia con más hechos para colección {}: {} con {} hechos ({}%)",
                coleccionId, provincia, cantidadHechos, String.format("%.2f", porcentaje));

        return dto;
    }

    private CategoriaStatsDTO calcularCategoriaDesdeBD() {
        log.info("Calculando categoría con más hechos");

        // 1. Obtener estadísticas por categoría desde BD
        List<Object[]> resultados = hechoRepository.findStatsPorCategoria();

        if (resultados.isEmpty()) {
            log.warn("No se encontraron hechos en el sistema");
            return CategoriaStatsDTO.builder()
                    .categoria("Sin datos")
                    .cantidadHechos(0L)
                    .porcentaje(0.0)
                    .fechaCalculo(LocalDateTime.now())
                    .build();
        }

        // 2. Obtener el primer resultado (el que tiene más hechos)
        Object[] primerResultado = resultados.get(0);
        String categoria = (String) primerResultado[0];
        Long cantidadHechos = (Long) primerResultado[1];

        // 3. Calcular porcentaje
        Long totalHechos = resultados.stream()
                .mapToLong(resultado -> (Long) resultado[1])
                .sum();

        Double porcentaje = totalHechos > 0 ?
                (cantidadHechos * 100.0 / totalHechos) : 0.0;

        // 4. Crear y retornar DTO
        CategoriaStatsDTO dto = CategoriaStatsDTO.builder()
                .categoria(categoria)
                .cantidadHechos(cantidadHechos)
                .porcentaje(porcentaje)
                .fechaCalculo(LocalDateTime.now())
                .build();

        log.info("Categoría con más hechos: {} con {} hechos ({}%)",
                categoria, cantidadHechos, String.format("%.2f", porcentaje));

        return dto;
    }

    private ProvinciaStatsDTO calcularProvinciaPorCategoriaDesdeBD(String categoria) {
        log.info("Calculando provincia con más hechos para categoría: {}", categoria);

        // 1. Obtener estadísticas por provincia y categoría desde BD
        List<Object[]> resultados = hechoRepository.findStatsPorProvinciaYCategoria(categoria);

        if (resultados.isEmpty()) {
            log.warn("No se encontraron hechos para la categoría: {}", categoria);
            return ProvinciaStatsDTO.builder()
                    .provincia("Sin datos")
                    .cantidadHechos(0L)
                    .porcentaje(0.0)
                    .fechaCalculo(LocalDateTime.now())
                    .categoria(categoria)
                    .build();
        }

        // 2. Obtener el primer resultado (el que tiene más hechos)
        Object[] primerResultado = resultados.get(0);
        String provincia = (String) primerResultado[0];
        Long cantidadHechos = (Long) primerResultado[1];

        // 3. Calcular porcentaje
        Long totalHechosCategoria = resultados.stream()
                .mapToLong(resultado -> (Long) resultado[1])
                .sum();

        Double porcentaje = totalHechosCategoria > 0 ?
                (cantidadHechos * 100.0 / totalHechosCategoria) : 0.0;

        // 4. Crear y retornar DTO
        ProvinciaStatsDTO dto = ProvinciaStatsDTO.builder()
                .provincia(provincia)
                .cantidadHechos(cantidadHechos)
                .porcentaje(porcentaje)
                .fechaCalculo(LocalDateTime.now())
                .categoria(categoria)
                .build();

        log.info("Provincia con más hechos para categoría {}: {} con {} hechos ({}%)",
                categoria, provincia, cantidadHechos, String.format("%.2f", porcentaje));

        return dto;
    }

    private HoraStatsDTO calcularHoraPorCategoriaDesdeBD(String categoria) {
        log.info("Calculando hora con más hechos para categoría: {}", categoria);

        // 1. Obtener estadísticas por hora y categoría desde BD
        List<Object[]> resultados = hechoRepository.findStatsPorHoraYCategoria(categoria);

        if (resultados.isEmpty()) {
            log.warn("No se encontraron hechos para la categoría: {}", categoria);
            return HoraStatsDTO.builder()
                    .categoria(categoria)
                    .hora(0)
                    .cantidadHechos(0L)
                    .porcentaje(0.0)
                    .fechaCalculo(LocalDateTime.now())
                    .build();
        }

        // 2. Obtener el primer resultado (el que tiene más hechos)
        Object[] primerResultado = resultados.get(0);
        Integer hora = (Integer) primerResultado[0];
        Long cantidadHechos = (Long) primerResultado[1];

        // 3. Calcular porcentaje
        Long totalHechosCategoria = resultados.stream()
                .mapToLong(resultado -> (Long) resultado[1])
                .sum();

        Double porcentaje = totalHechosCategoria > 0 ?
                (cantidadHechos * 100.0 / totalHechosCategoria) : 0.0;

        // 4. Crear y retornar DTO
        HoraStatsDTO dto = HoraStatsDTO.builder()
                .categoria(categoria)
                .hora(hora)
                .cantidadHechos(cantidadHechos)
                .porcentaje(porcentaje)
                .fechaCalculo(LocalDateTime.now())
                .build();

        log.info("Hora con más hechos para categoría {}: {}:00 con {} hechos ({}%)",
                categoria, hora, cantidadHechos, String.format("%.2f", porcentaje));

        return dto;
    }

    private SpamStatsDTO calcularSpamDesdeBD() {
        log.info("Calculando estadísticas de spam");

        try {
            // 1. Obtener estadísticas de spam desde BD
            Long totalSolicitudes = solicitudEliminacionRepository.countTotalSolicitudes();
            Long solicitudesSpam = solicitudEliminacionRepository.countSolicitudesSpam();
            Double porcentajeSpam = solicitudEliminacionRepository.calcularPorcentajeSpam();

            if (totalSolicitudes == null || totalSolicitudes == 0) {
                log.warn("No se encontraron solicitudes de eliminación en el sistema");
                return SpamStatsDTO.builder()
                        .totalSolicitudes(0L)
                        .solicitudesSpam(0L)
                        .solicitudesNoSpam(0L)
                        .porcentajeSpam(0.0)
                        .porcentajeNoSpam(100.0)
                        .fechaCalculo(LocalDateTime.now())
                        .build();
            }

            // 2. Calcular solicitudes no spam
            Long solicitudesNoSpam = totalSolicitudes - solicitudesSpam;
            Double porcentajeNoSpam = 100.0 - (porcentajeSpam != null ? porcentajeSpam : 0.0);

            // 3. Crear y retornar DTO
            SpamStatsDTO dto = SpamStatsDTO.builder()
                    .totalSolicitudes(totalSolicitudes)
                    .solicitudesSpam(solicitudesSpam)
                    .solicitudesNoSpam(solicitudesNoSpam)
                    .porcentajeSpam(porcentajeSpam != null ? porcentajeSpam : 0.0)
                    .porcentajeNoSpam(porcentajeNoSpam)
                    .fechaCalculo(LocalDateTime.now())
                    .build();

            log.info("Estadísticas de spam: {} total, {} spam ({}%), {} no spam ({}%)",
                    totalSolicitudes, solicitudesSpam, String.format("%.2f", porcentajeSpam != null ? porcentajeSpam : 0.0),
                    solicitudesNoSpam, String.format("%.2f", porcentajeNoSpam));

            return dto;

        } catch (Exception e) {
            log.error("Error al calcular estadísticas de spam: {}", e.getMessage());
            return SpamStatsDTO.builder()
                    .totalSolicitudes(0L)
                    .solicitudesSpam(0L)
                    .solicitudesNoSpam(0L)
                    .porcentajeSpam(0.0)
                    .porcentajeNoSpam(100.0)
                    .fechaCalculo(LocalDateTime.now())
                    .build();
        }
    }
}