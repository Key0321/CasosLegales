package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.Documento;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentoRepositorio extends JpaRepository<Documento, Long>, JpaSpecificationExecutor<Documento> {
    List<Documento> findByProcesoIdAndEliminadoPorIsNull(Long procesoId);

    Optional<Documento> findByIdAndFechaEliminacionIsNull(Long id);
    
    @Query("SELECT DISTINCT d.proceso FROM Documento d WHERE d.proceso.id IN :procesoIds")
    List<ProcesoLegal> findDistinctProcesosByProcesoIds(@Param("procesoIds") List<Long> procesoIds);

    @Query("SELECT DISTINCT d.anio FROM Documento d WHERE d.proceso.id IN :procesoIds")
    List<Integer> findDistinctAniosByProcesoIds(@Param("procesoIds") List<Long> procesoIds);

    @Query("SELECT DISTINCT d.tipoDocumento FROM Documento d WHERE d.proceso.id IN :procesoIds")
    List<String> findDistinctTiposByProcesoIds(@Param("procesoIds") List<Long> procesoIds);
}
