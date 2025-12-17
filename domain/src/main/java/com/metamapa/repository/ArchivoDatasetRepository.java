package com.metamapa.repository;

import com.metamapa.entities.archivosDataset.ArchivoDataset;
import com.metamapa.entities.archivosDataset.EstadoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchivoDatasetRepository extends JpaRepository<ArchivoDataset, Long> {
    
    Optional<ArchivoDataset> findByHash(String hash);
    
    List<ArchivoDataset> findByEstado(EstadoArchivo estado);
}
