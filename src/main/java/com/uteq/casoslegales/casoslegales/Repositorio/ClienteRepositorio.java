package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.Cliente;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    boolean existsByUsuario(Usuario usuario);
}
