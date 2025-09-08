package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Rol;
import com.uteq.casoslegales.casoslegales.Repositorio.RolRepositorio;
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
public class RolServicio {

    @Autowired
    private RolRepositorio rolRepositorio;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Rol> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return rolRepositorio.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (rolRepositorio.existsById(id)) {
            rolRepositorio.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Rol> listarTodos() {
        return rolRepositorio.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Rol> obtenerPorId(Long id) {
        return rolRepositorio.findById(id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Rol obtenerPorNombre(String nombre) throws Exception {
        return rolRepositorio.findByNombre(nombre)
                .orElseThrow(() -> new Exception("Rol no encontrado: " + nombre));
    }

    @Transactional(rollbackFor = Exception.class)
    public Rol guardar(Rol rol) {
        return rolRepositorio.save(rol);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        rolRepositorio.deleteById(id);
    }
}
