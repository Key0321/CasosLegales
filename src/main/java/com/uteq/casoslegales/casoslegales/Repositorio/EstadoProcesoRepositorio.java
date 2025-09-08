package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.EstadoProceso;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoProcesoRepositorio extends JpaRepository<EstadoProceso, Long> {
    Optional<EstadoProceso> findByNombre(String nombre);
}
