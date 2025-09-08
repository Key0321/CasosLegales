package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.ProcesoActividad;
import com.uteq.casoslegales.casoslegales.Repositorio.ProcesoActividadRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProcesoActividadServicio {

    @Autowired
    private ProcesoActividadRepositorio actividadRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ProcesoActividad> listarTodos() {
        return actividadRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<ProcesoActividad> obtenerPorId(Long id) {
        return actividadRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProcesoActividad guardar(ProcesoActividad actividad) {
        return actividadRepo.save(actividad);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        actividadRepo.deleteById(id);
    }
}
