package com.uteq.casoslegales.casoslegales.Repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uteq.casoslegales.casoslegales.Modelo.Usuario;

public interface UsuarioRepo extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByCorreo(String correo);

    @Query("SELECT u.id, u.identificacion, CONCAT(u.nombre, ' ', u.apellido) " +
       "FROM Usuario u ")
    List<Object[]> listarUsuarioCampos();

    @Query("SELECT u FROM Usuario u WHERE u.creadoPor.id = :creadorId AND " +
           "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.correo) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Usuario> findByCreadorIdAndBusqueda(@Param("creadorId") Long creadorId, 
                                           @Param("busqueda") String busqueda, 
                                           Pageable pageable);
    
    // Para cuando no hay búsqueda
    Page<Usuario> findByCreadoPor_Id(Long creadorId, Pageable pageable);
}
