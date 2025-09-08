package com.uteq.casoslegales.casoslegales.Repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uteq.casoslegales.casoslegales.Modelo.Usuario;

public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    @Query("SELECT u.id, u.identificacion, CONCAT(u.nombre, ' ', u.apellido) " +
       "FROM Usuario u ")
    List<Object[]> listarUsuarioCampos();

}
