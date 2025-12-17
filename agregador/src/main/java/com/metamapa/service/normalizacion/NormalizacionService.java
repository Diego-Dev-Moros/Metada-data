package com.metamapa.service.normalizacion;

import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.ubicaciones.Ubicacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Normaliza:
 * - Fecha → "aa/MM/dd" (dos dígitos de año)
 * - Ubicación → lat/lon redondeados
 * - Categoría → vía taxonomía (resolver)
 * - Título → texto básico canónico
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NormalizacionService {

    private static final int DECIMALES_COORDENADAS = 5;
    private static final DateTimeFormatter YY_MM_DD = DateTimeFormatter.ofPattern("yy/MM/dd");

    // Resolver de taxonomía: devuelve ruta canónica, p.ej. "ambiental/fuego/incendio_forestal"
    private final CategoriaResolverTaxonomico categoriaResolver;

    public List<Hecho> normalizar(List<Hecho> hechos) {
        return hechos.stream().map(this::normalizarHecho).collect(Collectors.toList());
    }

    private Hecho normalizarHecho(Hecho h) {
        // ── Título (básico)
        if (h.getTitulo() != null) {
            h.setTitulo(normalizarTextoBasico(h.getTitulo()));
        }

        // ── Categoría (taxonomía)
        // Usa título + (opcional) descripción/categoría original como señales
        String categoriaCanonica = categoriaResolver.clasificar(
                h.getTitulo(),                   // texto principal
                safeString(h.getDescripcion()),  // si existe
                safeString(h.getCategoria())     // etiqueta/fuente original (si existe)
        );
        h.setCategoria(categoriaCanonica);

        // ── Fecha → "aa/MM/dd"
        // Lee cualquier tipo razonable y lo deja en formato string "aa/MM/dd"
        LocalDateTime fechaYYMMDD = normalizarFecha_aaMMdd(h.getFechaHecho()); // h.getFecha() puede ser String/LocalDate/LocalDateTime/Date
        h.setFechaHecho(fechaYYMMDD); // asegurate de que Hecho tenga un setter compatible (String). Si no, crea un campo fechaCanonica.

        // ── Ubicación → redondeo
        if (h.getUbicacion() != null) {
            h.setUbicacion(normalizarUbicacion(h.getUbicacion()));
        }

        return h;
    }
    private static String safeString(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // FECHA → "aa/MM/dd"
    // Acepta LocalDate, LocalDateTime, java.util.Date o String ISO "yyyy-MM-dd" / "yyyy/MM/dd"
    private LocalDateTime normalizarFecha_aaMMdd(Object fecha) {
        if (fecha == null) return null;

        LocalDate ld;

        if (fecha instanceof LocalDate) {
            ld = (LocalDate) fecha;
        } else if (fecha instanceof LocalDateTime) {
            ld = ((LocalDateTime) fecha).toLocalDate();
        } else if (fecha instanceof java.util.Date) {
            ld = ((java.util.Date) fecha).toInstant().atZone(java.time.ZoneId.of("UTC")).toLocalDate();
        } else {
            // String u otro objeto → parse básico
            String s = String.valueOf(fecha).trim();

            // ISO yyyy-MM-dd
            try {
                ld = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception ignored) {
                // yyyy/MM/dd
                try {
                    ld = LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                } catch (Exception ignored2) {
                    // yyyy-M-d
                    if (s.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
                        String[] p = s.split("-");
                        String y = p[0];
                        String m = String.format("%02d", Integer.parseInt(p[1]));
                        String d = String.format("%02d", Integer.parseInt(p[2]));
                        ld = LocalDate.parse(y + "-" + m + "-" + d, DateTimeFormatter.ISO_LOCAL_DATE);
                    } else if (s.matches("^\\d{4}/\\d{1,2}/\\d{1,2}$")) {
                        String[] p = s.split("/");
                        String y = p[0];
                        String m = String.format("%02d", Integer.parseInt(p[1]));
                        String d = String.format("%02d", Integer.parseInt(p[2]));
                        ld = LocalDate.parse(y + "/" + m + "/" + d, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    } else {
                        throw new IllegalArgumentException("Formato de fecha no soportado: " + s);
                    }
                }
            }
        }

        return ld.atStartOfDay();
    }


    // ─────────────────────────────────────────────────────────────────────────────
    // UBICACIÓN → redondeo lat/lon
    private Ubicacion normalizarUbicacion(Ubicacion u) {
        Ubicacion out = new Ubicacion();
        out.setLatitud(roundTo(u.getLatitud(), DECIMALES_COORDENADAS));
        out.setLongitud(roundTo(u.getLongitud(), DECIMALES_COORDENADAS));
        // Si tu Ubicacion tiene otros campos, copialos acá si corresponde.
        return out;
    }

    private static double roundTo(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Normaliza acentos, espacios, mayúsculas → string “canónico” para comparar
    public static String normalizarTextoBasico(String s) {
        String t = s.trim().toLowerCase(Locale.ROOT);
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", ""); // saca acentos
        t = t.replaceAll("\\s+", " "); // colapsa espacios
        return t;
    }


    // Construye el fingerprint a partir de los campos ya normalizados
    private String buildFingerprint(Hecho h) {
        // Evitamos NPE con valores nulos y normalizamos
        final String titulo = h.getTitulo() != null ? normalizarTextoBasico(h.getTitulo()) : "";
        final String categoria = h.getCategoria() != null ? normalizarTextoBasico(h.getCategoria()) : "";

        String fecha = "";
        if (h.getFechaHecho() != null) {
            // Usamos sólo la fecha (sin hora), con formato "yy/MM/dd"
            fecha = h.getFechaHecho().format(DateTimeFormatter.ofPattern("yy/MM/dd"));
        }

        String lat = "";
        String lon = "";
        if (h.getUbicacion() != null) {
            lat = String.format(Locale.ROOT, "%.3f", h.getUbicacion().getLatitud());
            lon = String.format(Locale.ROOT, "%.3f", h.getUbicacion().getLongitud());
        }

        // Concatenamos en orden canónico → facilita comparación
        return titulo + "|" + categoria + "|" + fecha + "|" + lat + "|" + lon;
    }


}


