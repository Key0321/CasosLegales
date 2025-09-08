package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProcesoLegalRepositorio extends JpaRepository<ProcesoLegal, Long> {
    @Query("SELECT p.id, p.numeroProceso, CONCAT(c.usuario.nombre, ' ', c.usuario.apellido) " +
       "FROM ProcesoLegal p " +
       "JOIN p.cliente c")
    List<Object[]> listarProcesosSoloCampos();

    Optional<ProcesoLegal> findByIdAndEliminadoPorIsNull(Long id);


    @Query("SELECT p FROM ProcesoLegal p JOIN p.usuarios pu WHERE pu.usuario.id = :usuarioId")
    List<ProcesoLegal> findProcesosLegalesByUsuarioId(Long usuarioId);

    @Query("SELECT p FROM ProcesoLegal p WHERE p.cliente.usuario.id = :usuarioId")
    List<ProcesoLegal> findProcesosLegalesByClienteUsuarioId(Long usuarioId);
}
