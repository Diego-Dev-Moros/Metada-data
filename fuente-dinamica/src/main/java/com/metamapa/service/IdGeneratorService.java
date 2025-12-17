package com.metamapa.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio simple para generar IDs únicos para MongoDB
 */
@Service
public class IdGeneratorService {
    
    private final AtomicLong hechoIdCounter = new AtomicLong(1);
    private final AtomicLong contribuyenteIdCounter = new AtomicLong(1);
    
    public Long nextHechoId() {
        return hechoIdCounter.getAndIncrement();
    }
    
    public Long nextContribuyenteId() {
        return contribuyenteIdCounter.getAndIncrement();
    }
}