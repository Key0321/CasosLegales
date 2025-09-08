package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.Acceso;
import com.uteq.casoslegales.casoslegales.Servicio.AccesoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accesos")
@CrossOrigin("*")
public class AccesoControlador {

    @Autowired
    private AccesoServicio accesoServicio;

    @GetMapping
    public List<Acceso> listarTodos() {
        return accesoServicio.listarTodos();
    }

    @GetMapping("/{id}")
    public Optional<Acceso> obtenerPorId(@PathVariable Long id) {
        return accesoServicio.obtenerPorId(id);
    }

    @PostMapping
    public Acceso guardar(@RequestBody Acceso acceso) {
        return accesoServicio.guardar(acceso);
    }

    @PutMapping("/{id}")
    public Acceso actualizar(@PathVariable Long id, @RequestBody Acceso acceso) {
        acceso.setId(id);
        return accesoServicio.guardar(acceso);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        accesoServicio.eliminar(id);
    }
}
