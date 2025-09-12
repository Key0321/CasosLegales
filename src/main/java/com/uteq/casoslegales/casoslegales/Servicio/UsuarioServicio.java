package com.uteq.casoslegales.casoslegales.Servicio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ProcesoUsuarioRepositorio;
import com.uteq.casoslegales.casoslegales.Repositorio.UsuarioRepo;

@Service
@Transactional
public class UsuarioServicio {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProcesoUsuarioRepositorio procesoUsuarioRepositorio;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Usuario> autenticar(String correo, String contrasenia) {
        return usuarioRepo.findByCorreo(correo)
                .filter(u -> 
                    passwordEncoder.matches(contrasenia, u.getContrasenia()) 
                    || u.getContrasenia().equals(contrasenia)
                );
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Usuario> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return usuarioRepo.findAll(pageable);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepo.findByCorreo(email);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Usuario> listarNoInvolucradosEnProceso(Long procesoId) {
        // Obtener IDs de usuarios involucrados en el proceso
        List<Long> usuariosInvolucradosIds = procesoUsuarioRepositorio
                .findByProcesoIdAndEliminadoPorIsNull(procesoId)
                .stream()
                .map(pu -> pu.getUsuario().getId())
                .collect(Collectors.toList());

        // Listar todos los usuarios que no estén en la lista de involucrados
        return usuarioRepo.findAll()
                .stream()
                .filter(usuario -> !usuariosInvolucradosIds.contains(usuario.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (usuarioRepo.existsById(id)) {
            usuarioRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Object[]> listarUsuarioMinimo() {
        return usuarioRepo.listarUsuarioCampos();
    }

    @Transactional(rollbackFor = Exception.class)
    public Usuario guardar(Usuario usuario) {
        return usuarioRepo.save(usuario);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Usuario> listarTodos() {
        return usuarioRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        usuarioRepo.deleteById(id);
    }

   public Page<Usuario> listarPaginadosConFiltro(String busqueda, Long creadorId, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano);
        
        if (busqueda != null && !busqueda.isEmpty()) {
            return usuarioRepo.findByCreadorIdAndBusqueda(creadorId, busqueda, pageable);
        } else {
            return usuarioRepo.findByCreadoPor_Id(creadorId, pageable);
        }
    }

}
