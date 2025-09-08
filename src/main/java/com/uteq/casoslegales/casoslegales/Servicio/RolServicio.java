package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Rol;
import com.uteq.casoslegales.casoslegales.Repositorio.RolRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolServicio {

    @Autowired
    private RolRepositorio rolRepositorio;

    public Page<Rol> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return rolRepositorio.findAll(pageable);
    }

    public boolean eliminarPorId(Long id) {
        if (rolRepositorio.existsById(id)) {
            rolRepositorio.deleteById(id);
            return true;
        }
        return false;
    }


    public List<Rol> listarTodos() {
        return rolRepositorio.findAll();
    }

    public Optional<Rol> obtenerPorId(Long id) {
        return rolRepositorio.findById(id);
    }

    public Rol obtenerPorNombre(String nombre) throws Exception {
        return rolRepositorio.findByNombre(nombre)
                .orElseThrow(() -> new Exception("Rol no encontrado: " + nombre));
    }

    public Rol guardar(Rol rol) {
        return rolRepositorio.save(rol);
    }

    public void eliminar(Long id) {
        rolRepositorio.deleteById(id);
    }
}
