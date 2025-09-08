package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.ProcesoUsuario;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoUsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/proceso-usuarios")
@CrossOrigin("*")
public class ProcesoUsuarioControlador {

    @Autowired
    private ProcesoUsuarioServicio procesoUsuarioServicio;

    @GetMapping
    public List<ProcesoUsuario> listarTodos() {
        return procesoUsuarioServicio.listarTodos();
    }

    @GetMapping("/{id}")
    public Optional<ProcesoUsuario> obtenerPorId(@PathVariable Long id) {
        return procesoUsuarioServicio.obtenerPorId(id);
    }

    @PostMapping
    public ProcesoUsuario guardar(@RequestBody ProcesoUsuario procesoUsuario) {
        return procesoUsuarioServicio.guardar(procesoUsuario);
    }

    @PutMapping("/{id}")
    public ProcesoUsuario actualizar(@PathVariable Long id, @RequestBody ProcesoUsuario procesoUsuario) {
        procesoUsuario.setId(id);
        return procesoUsuarioServicio.guardar(procesoUsuario);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        procesoUsuarioServicio.eliminar(id);
    }
}
