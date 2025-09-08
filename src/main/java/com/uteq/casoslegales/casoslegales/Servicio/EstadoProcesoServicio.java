package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.EstadoProceso;
import com.uteq.casoslegales.casoslegales.Repositorio.EstadoProcesoRepositorio;
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
public class EstadoProcesoServicio {

    @Autowired
    private EstadoProcesoRepositorio estadoRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<EstadoProceso> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return estadoRepo.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (estadoRepo.existsById(id)) {
            estadoRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<EstadoProceso> listarTodos() {
        return estadoRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<EstadoProceso> obtenerPorId(Long id) {
        return estadoRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public EstadoProceso guardar(EstadoProceso estado) {
        return estadoRepo.save(estado);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<EstadoProceso> buscarPorNombre(String nombre) {
        return estadoRepo.findByNombre(nombre);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        estadoRepo.deleteById(id);
    }
}
