package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Permiso;
import com.uteq.casoslegales.casoslegales.Repositorio.PermisoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PermisoServicio {

    @Autowired
    private PermisoRepositorio permisoRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Permiso> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return permisoRepo.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (permisoRepo.existsById(id)) {
            permisoRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Permiso> listarTodos() {
        return permisoRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Permiso> obtenerPorId(Long id) {
        return permisoRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Permiso guardar(Permiso permiso) {
        return permisoRepo.save(permiso);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        permisoRepo.deleteById(id);
    }
}
