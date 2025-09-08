package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Observacion;
import com.uteq.casoslegales.casoslegales.Repositorio.ObservacionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ObservacionServicio {

    @Autowired
    private ObservacionRepositorio observacionRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Observacion> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return observacionRepo.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (observacionRepo.existsById(id)) {
            observacionRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Observacion> listarTodos() {
        return observacionRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Observacion> obtenerPorId(Long id) {
        return observacionRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Observacion guardar(Observacion observacion) {
        return observacionRepo.save(observacion);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        observacionRepo.deleteById(id);
    }
}
