package com.metamapa.scheduler;

import com.metamapa.service.FuenteDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FuenteDemoScheduler {
    private final FuenteDemoService fuenteDemoService;

    @Scheduled(fixedRate = 3600000) // Cada hora
    public void consultarNuevosHechos() {
        log.info("Consultando nuevos hechos de la fuente demo...");
        fuenteDemoService.consultarNuevosHechos();
    }
} 