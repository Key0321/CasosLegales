package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Abogado;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.AbogadoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AbogadoServicio {

    @Autowired
    private AbogadoRepositorio abogadoRepo;

    public Page<Abogado> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return abogadoRepo.findAll(pageable);
    }

    public boolean eliminarPorId(Long id) {
        if (abogadoRepo.existsById(id)) {
            abogadoRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existePorUsuarioId(Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        return abogadoRepo.existsByUsuario(usuario);
    }

    public List<Abogado> listarTodos() {
        return abogadoRepo.findAll();
    }

    public Optional<Abogado> obtenerPorId(Long id) {
        return abogadoRepo.findById(id);
    }

    public Abogado guardar(Abogado abogado) {
        return abogadoRepo.save(abogado);
    }

    public void eliminar(Long id) {
        abogadoRepo.deleteById(id);
    }
}
