package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.ProcesoActividad;
import com.uteq.casoslegales.casoslegales.Repositorio.ProcesoActividadRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProcesoActividadServicio {

    @Autowired
    private ProcesoActividadRepositorio actividadRepo;

    public List<ProcesoActividad> listarTodos() {
        return actividadRepo.findAll();
    }

    public Optional<ProcesoActividad> obtenerPorId(Long id) {
        return actividadRepo.findById(id);
    }

    public ProcesoActividad guardar(ProcesoActividad actividad) {
        return actividadRepo.save(actividad);
    }

    public void eliminar(Long id) {
        actividadRepo.deleteById(id);
    }
}
