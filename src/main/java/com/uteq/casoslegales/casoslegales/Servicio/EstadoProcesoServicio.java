package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.EstadoProceso;
import com.uteq.casoslegales.casoslegales.Repositorio.EstadoProcesoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstadoProcesoServicio {

    @Autowired
    private EstadoProcesoRepositorio estadoRepo;

    public Page<EstadoProceso> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return estadoRepo.findAll(pageable);
    }

    public boolean eliminarPorId(Long id) {
        if (estadoRepo.existsById(id)) {
            estadoRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<EstadoProceso> listarTodos() {
        return estadoRepo.findAll();
    }

    public Optional<EstadoProceso> obtenerPorId(Long id) {
        return estadoRepo.findById(id);
    }

    public EstadoProceso guardar(EstadoProceso estado) {
        return estadoRepo.save(estado);
    }

    public Optional<EstadoProceso> buscarPorNombre(String nombre) {
        return estadoRepo.findByNombre(nombre);
    }


    public void eliminar(Long id) {
        estadoRepo.deleteById(id);
    }
}
