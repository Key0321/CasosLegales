package com.uteq.casoslegales.casoslegales.Servicio;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ProcesoUsuarioRepositorio;
import com.uteq.casoslegales.casoslegales.Repositorio.UsuarioRepo;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProcesoUsuarioRepositorio procesoUsuarioRepositorio;

    public Optional<Usuario> autenticar(String correo, String contrasenia) {
    return usuarioRepo.findByCorreo(correo)
            .filter(u -> 
                passwordEncoder.matches(contrasenia, u.getContrasenia()) 
                || u.getContrasenia().equals(contrasenia)
            );
}

    public Page<Usuario> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return usuarioRepo.findAll(pageable);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepo.findByCorreo(email);
    }

    @Transactional(readOnly = true)
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

    public boolean eliminarPorId(Long id) {
        if (usuarioRepo.existsById(id)) {
            usuarioRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Object[]> listarUsuarioMinimo() {
        return usuarioRepo.listarUsuarioCampos();
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepo.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepo.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepo.findById(id);
    }

    public void eliminar(Long id) {
        usuarioRepo.deleteById(id);
    }
}
