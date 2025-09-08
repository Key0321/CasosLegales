package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Observacion;
import com.uteq.casoslegales.casoslegales.Repositorio.ObservacionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObservacionServicio {

    @Autowired
    private ObservacionRepositorio observacionRepo;

    public Page<Observacion> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return observacionRepo.findAll(pageable);
    }

    public boolean eliminarPorId(Long id) {
        if (observacionRepo.existsById(id)) {
            observacionRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Observacion> listarTodos() {
        return observacionRepo.findAll();
    }

    public Optional<Observacion> obtenerPorId(Long id) {
        return observacionRepo.findById(id);
    }

    public Observacion guardar(Observacion observacion) {
        return observacionRepo.save(observacion);
    }

    public void eliminar(Long id) {
        observacionRepo.deleteById(id);
    }
}
