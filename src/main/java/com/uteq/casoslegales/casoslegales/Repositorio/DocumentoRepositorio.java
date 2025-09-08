package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.Documento;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepositorio extends JpaRepository<Documento, Long> {
    List<Documento> findByProcesoIdAndEliminadoPorIsNull(Long procesoId);

    Optional<Documento> findByIdAndFechaEliminacionIsNull(Long id);
}
