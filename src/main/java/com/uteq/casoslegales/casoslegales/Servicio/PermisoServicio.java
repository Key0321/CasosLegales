package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Permiso;
import com.uteq.casoslegales.casoslegales.Repositorio.PermisoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermisoServicio {

    @Autowired
    private PermisoRepositorio permisoRepo;

    public Page<Permiso> listarPaginados(int pagina, int tamaño) {
    Pageable pageable = PageRequest.of(pagina, tamaño);
        return permisoRepo.findAll(pageable);
    }

    public boolean eliminarPorId(Long id) {
        if (permisoRepo.existsById(id)) {
            permisoRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Permiso> listarTodos() {
        return permisoRepo.findAll();
    }


    public Optional<Permiso> obtenerPorId(Long id) {
        return permisoRepo.findById(id);
    }

    public Permiso guardar(Permiso permiso) {
        return permisoRepo.save(permiso);
    }

    public void eliminar(Long id) {
        permisoRepo.deleteById(id);
    }
}
