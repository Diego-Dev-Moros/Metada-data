package com.metamapa.config;

import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.service.ServicioAgregacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FuenteRegistrationConfig implements CommandLineRunner {
    
    private final ServicioAgregacion servicioAgregacion;
    private final List<FuenteDeDatos> fuentesDeDatos;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Registrando fuentes en ServicioAgregacion...");
        
        for (FuenteDeDatos fuente : fuentesDeDatos) {
            servicioAgregacion.registrarFuente(fuente);
            log.info("Fuente registrada: {} (tipo: {})", fuente.getIdentificador(), fuente.getTipo());
        }
        
        log.info("Total de fuentes registradas: {}", fuentesDeDatos.size());
    }
}
