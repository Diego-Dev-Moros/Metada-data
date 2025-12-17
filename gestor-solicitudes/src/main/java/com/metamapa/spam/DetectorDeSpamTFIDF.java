package com.metamapa.spam;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DetectorDeSpamTFIDF implements DetectorDeSpam {

    // Lista única de palabras y frases de spam
    private final List<String> palabrasSpam = Arrays.asList(
            "comprar", "gratis", "dinero", "ganar", "millones", "urgente",
            "oferta", "descuento", "increíble", "increible", "ganancias",
            "trabajo desde casa", "hacer dinero", "riqueza", "fortuna",
            "click aquí", "click aqui", "visita ahora", "actúa ya", "actua ya",
            "haz clic aquí", "suscríbete ahora", "promoción limitada",
            "solo hoy", "gana dinero rápido", "premio gratis"
    );

    private final Map<String, Double> idfScores;

    public DetectorDeSpamTFIDF() {
        idfScores = calcularIDF(palabrasSpam);
    }

    @Override
    public boolean esSpam(String texto) {
        if (texto == null || texto.trim().isEmpty()) return false;

        String textoLower = texto.toLowerCase();

        // 1️⃣ Reglas básicas con palabras y frases spam
        int contadorSpam = 0;
        for (String palabra : palabrasSpam) {
            if (textoLower.contains(palabra.toLowerCase())) {
                contadorSpam++;
            }
        }
        if (contadorSpam >= 2) return true;

        // 2️⃣ Repetición de caracteres (aaaa, !!!!, etc.)
        if (textoLower.matches(".*([a-z])\\1{4,}.*")) return true;

        // 3️⃣ Score TF-IDF usando palabras y frases
        Map<String, Double> tf = calcularTF(textoLower);
        double score = 0.0;
        for (String palabra : tf.keySet()) {
            if (idfScores.containsKey(palabra)) {
                score += tf.get(palabra) * idfScores.get(palabra);
            }
        }

        return score > 0.3; // umbral ajustable para ser más sensible
    }

    private Map<String, Double> calcularTF(String texto) {
        Map<String, Double> tf = new HashMap<>();
        for (String palabra : palabrasSpam) {
            if (texto.contains(palabra.toLowerCase())) {
                tf.put(palabra.toLowerCase(), 1.0);
            }
        }
        return tf;
    }

    private Map<String, Double> calcularIDF(List<String> listaPalabras) {
        Map<String, Double> idf = new HashMap<>();
        int totalDocs = listaPalabras.size();
        for (String palabra : listaPalabras) {
            idf.put(palabra.toLowerCase(), Math.log((double) totalDocs / (1 + 1)));
        }
        return idf;
    }
}
