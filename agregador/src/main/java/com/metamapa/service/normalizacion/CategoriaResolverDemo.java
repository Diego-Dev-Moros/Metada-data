package com.metamapa.service.normalizacion;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;

/**
 * Demo taxonómica simple (Java 8 compatible).
 * - Selecciona macro → subrama → hoja usando conteo de keywords.
 * - Si no alcanza umbrales, cae en "otros/desconocido".
 *
 * Activa sólo con el perfil "demo":
 *   SPRING_PROFILES_ACTIVE=demo
 */
@Component
@Profile("demo")
public class CategoriaResolverDemo implements CategoriaResolverTaxonomico {

    private static final String OTROS_GLOBAL = "otros/desconocido";
    private static final int UMBRAL_MACRO = 1;  // mínimo de coincidencias para macro
    private static final int UMBRAL_SUB   = 1;  // mínimo para subrama/hoja

    /**
     * Estructura: macro → subrama → hoja → keywords
     * (En producción, idealmente cargar desde JSON/BD en vez de hardcodear)
     */
    private final Map<String, Map<String, Map<String, List<String>>>> TAX = buildTaxonomiaDemo();

    @Override
    public String clasificar(String titulo, String descripcion, String categoriaOriginal) {
        // 1) Texto unificado y normalizado
        String texto = normalize(joinNonNull(titulo, " ", descripcion, " ", categoriaOriginal));
        if (isBlank(texto)) return OTROS_GLOBAL;

        // 2) Macro con mayor score
        String macro = null;
        int bestMacroScore = Integer.MIN_VALUE;

        for (Map.Entry<String, Map<String, Map<String, List<String>>>> e : TAX.entrySet()) {
            int s = scoreKeywords(texto, collectKeywords(e.getValue()));
            if (s > bestMacroScore) {
                bestMacroScore = s;
                macro = e.getKey();
            }
        }
        if (macro == null || bestMacroScore < UMBRAL_MACRO) return OTROS_GLOBAL;

        // 3) Subrama dentro de macro
        Map<String, Map<String, List<String>>> subramas = TAX.get(macro);
        String sub = null;
        int bestSubScore = Integer.MIN_VALUE;

        for (Map.Entry<String, Map<String, List<String>>> e : subramas.entrySet()) {
            int s = scoreKeywords(texto, collectKeywords(e.getValue()));
            if (s > bestSubScore) {
                bestSubScore = s;
                sub = e.getKey();
            }
        }
        if (sub == null || bestSubScore < UMBRAL_SUB) {
            return macro + "/otros";
        }

        // 4) Hoja dentro de subrama
        Map<String, List<String>> hojas = subramas.get(sub);
        String hoja = null;
        int bestHojaScore = Integer.MIN_VALUE;

        for (Map.Entry<String, List<String>> e : hojas.entrySet()) {
            int s = scoreKeywords(texto, e.getValue());
            if (s > bestHojaScore) {
                bestHojaScore = s;
                hoja = e.getKey();
            }
        }
        if (hoja == null || bestHojaScore < UMBRAL_SUB) {
            return macro + "/" + sub + "/otros";
        }

        return macro + "/" + sub + "/" + hoja;
    }

    // ───────────────────────────── Helpers ─────────────────────────────

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String joinNonNull(Object... parts) {
        StringBuilder sb = new StringBuilder();
        for (Object p : parts) {
            if (p == null) continue;
            String s = String.valueOf(p);
            if (s.trim().isEmpty()) continue;
            sb.append(s);
        }
        return sb.toString();
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.toLowerCase(Locale.ROOT).trim();
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        t = t.replaceAll("\\s+", " ");
        return t;
    }

    /**
     * Junta todos los keywords de un subárbol (recorre subramas y hojas).
     */
    private static List<String> collectKeywords(Map<String, ? extends Object> nodo) {
        List<String> out = new ArrayList<String>();
        for (Map.Entry<String, ? extends Object> e : nodo.entrySet()) {
            Object val = e.getValue();
            if (val instanceof Map) {
                // Recurse
                @SuppressWarnings("unchecked")
                Map<String, ? extends Object> m = (Map<String, ? extends Object>) val;
                out.addAll(collectKeywords(m));
            } else if (val instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> lst = (List<Object>) val;
                for (Object k : lst) {
                    if (k != null) out.add(normalize(String.valueOf(k)));
                }
            }
        }
        return out;
    }

    /**
     * Score simple: suma 1 por cada keyword contenida en el texto.
     * (Se puede mejorar con pesos, n-grams, similitud semántica, etc.)
     */
    private static int scoreKeywords(String texto, List<String> keywords) {
        int s = 0;
        for (String k : keywords) {
            if (k != null && !k.isEmpty() && texto.contains(k)) s++;
        }
        return s;
    }

    private static Map<String, Map<String, Map<String, List<String>>>> buildTaxonomiaDemo() {
        // Para Java 8, construir explícitamente sin double-brace initialization pesado.
        Map<String, Map<String, Map<String, List<String>>>> tax = new LinkedHashMap<String, Map<String, Map<String, List<String>>>>();

        // ── AMBIENTAL
        Map<String, Map<String, List<String>>> ambiental = new LinkedHashMap<String, Map<String, List<String>>>();
        // sub: FUEGO
        Map<String, List<String>> fuego = new LinkedHashMap<String, List<String>>();
        fuego.put("incendio_forestal", Arrays.asList(
                "incendio forestal", "fuego forestal", "bosque", "brigadista", "columna de humo"
        ));
        fuego.put("incendio_urbano", Arrays.asList(
                "incendio", "edificio", "departamento", "barrio", "bomberos"
        ));
        fuego.put("quema_controlada", Arrays.asList(
                "quema controlada", "quema autorizada", "contrafuego"
        ));
        ambiental.put("fuego", fuego);

        // sub: HIDROMETEO
        Map<String, List<String>> hidrometeo = new LinkedHashMap<String, List<String>>();
        hidrometeo.put("inundacion", Arrays.asList(
                "inundacion", "anegamiento", "crecida", "desborde"
        ));
        hidrometeo.put("tormenta_severa", Arrays.asList(
                "tormenta", "granizo", "vientos fuertes", "alerta meteorologica"
        ));
        ambiental.put("hidrometeo", hidrometeo);

        tax.put("ambiental", ambiental);

        // ── SEGURIDAD
        Map<String, Map<String, List<String>>> seguridad = new LinkedHashMap<String, Map<String, List<String>>>();
        // sub: ACCIDENTE_VIAL
        Map<String, List<String>> accVial = new LinkedHashMap<String, List<String>>();
        accVial.put("choque_multiple", Arrays.asList(
                "choque multiple", "colision vehicular", "autopista", "pile up"
        ));
        accVial.put("vuelco", Arrays.asList(
                "vuelco", "accidente vehicular", "camion volcado"
        ));
        seguridad.put("accidente_vial", accVial);

        // sub: DELITOS
        Map<String, List<String>> delitos = new LinkedHashMap<String, List<String>>();
        delitos.put("robo", Arrays.asList(
                "robo", "asalto", "arma de fuego", "arrebatador"
        ));
        delitos.put("homicidio", Arrays.asList(
                "homicidio", "asesinato"
        ));
        seguridad.put("delitos", delitos);

        tax.put("seguridad", seguridad);

        return tax;
    }
}
