package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.AudienciaEvento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AudienciaEventoRepositorio extends JpaRepository<AudienciaEvento, Long> {
    List<AudienciaEvento> findByProcesoIdAndEliminadoPorIsNull(Long procesoId);

    @Query("SELECT ae FROM AudienciaEvento ae JOIN FETCH ae.proceso WHERE ae.proceso.id IN :procesoIds AND ae.eliminadoPor IS NULL")
    List<AudienciaEvento> findByProcesoIdInAndEliminadoPorIsNull(@Param("procesoIds") List<Long> procesoIds);

    @Query("SELECT ae FROM AudienciaEvento ae JOIN FETCH ae.proceso WHERE ae.proceso.id IN :procesoIds AND ae.fechaEvento BETWEEN :start AND :end")
    List<AudienciaEvento> findByProcesoIdInAndFechaEventoBetween(
            @Param("procesoIds") List<Long> procesoIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Optional<AudienciaEvento> findByIdAndFechaEliminacionIsNull(Long id);
}