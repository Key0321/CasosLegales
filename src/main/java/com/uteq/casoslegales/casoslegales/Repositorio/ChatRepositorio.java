package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.Chat;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepositorio extends JpaRepository<Chat, Long> {
    Optional<Chat> findByProcesoIdAndEliminadoPorIsNull(Long procesoId);
}
