package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoUsuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcesoUsuarioRepositorio extends JpaRepository<ProcesoUsuario, Long> {
    @Query("SELECT DISTINCT p FROM ProcesoUsuario pu " +
       "JOIN pu.proceso p " +
       "JOIN FETCH p.estado e " +
       "JOIN FETCH p.cliente c " +
       "JOIN FETCH c.usuario u " +
       "WHERE pu.usuario.id = :usuarioId")
    List<ProcesoLegal> findProcesosLegalesByUsuarioId(@Param("usuarioId") Long usuarioId);

    boolean existsByProcesoIdAndUsuarioIdAndEliminadoPorIsNull(Long procesoId, Long usuarioId);
    List<ProcesoUsuario> findByProcesoIdAndEliminadoPorIsNull(Long procesoId);

   @Query("SELECT pu FROM ProcesoUsuario pu WHERE pu.usuario.id = :usuarioId AND pu.eliminadoPor IS NULL")
    List<ProcesoUsuario> findByUsuarioIdAndEliminadoPorIsNull(@Param("usuarioId") Long usuarioId);

    Optional<ProcesoUsuario> findByProcesoIdAndUsuarioIdAndFechaEliminacionIsNull(Long procesoId, Long usuarioId);

    List<ProcesoUsuario> findByProcesoIdAndFechaEliminacionIsNull(Long procesoId);

    boolean existsByProcesoIdAndUsuarioIdAndFechaEliminacionIsNull(Long procesoId, Long usuarioId);
}
