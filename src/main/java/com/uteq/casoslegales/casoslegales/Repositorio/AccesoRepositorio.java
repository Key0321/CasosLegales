package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.Acceso;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccesoRepositorio extends JpaRepository<Acceso, Long> {
    @Query("SELECT a FROM Acceso a WHERE a.usuario = :usuario ORDER BY a.fechaIngreso DESC")
    List<Acceso> findUltimoAccesoByUsuario(@Param("usuario") Usuario usuario, Pageable pageable);

    Acceso findTopByUsuarioIdOrderByFechaIngresoDesc(Long usuarioId);
}
