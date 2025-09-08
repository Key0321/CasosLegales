package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.ProcesoActividad;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoActividadServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/proceso-actividades")
@CrossOrigin("*")
public class ProcesoActividadControlador {

    @Autowired
    private ProcesoActividadServicio actividadServicio;

    @GetMapping
    public List<ProcesoActividad> listarTodos() {
        return actividadServicio.listarTodos();
    }

    @GetMapping("/{id}")
    public Optional<ProcesoActividad> obtenerPorId(@PathVariable Long id) {
        return actividadServicio.obtenerPorId(id);
    }

    @PostMapping
    public ProcesoActividad guardar(@RequestBody ProcesoActividad actividad) {
        return actividadServicio.guardar(actividad);
    }

    @PutMapping("/{id}")
    public ProcesoActividad actualizar(@PathVariable Long id, @RequestBody ProcesoActividad actividad) {
        actividad.setId(id);
        return actividadServicio.guardar(actividad);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        actividadServicio.eliminar(id);
    }
}
