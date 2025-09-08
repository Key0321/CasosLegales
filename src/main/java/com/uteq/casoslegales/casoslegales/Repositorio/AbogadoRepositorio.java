package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.Abogado;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AbogadoRepositorio extends JpaRepository<Abogado, Long> {
    boolean existsByUsuario(Usuario usuario);
}
