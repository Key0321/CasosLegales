package com.uteq.casoslegales.casoslegales.Controlador;


import com.uteq.casoslegales.casoslegales.Modelo.Observacion;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.ObservacionServicio;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class ObservacionControlador {

    @Autowired
    private ObservacionServicio observacionServicio;

    @GetMapping("/admin/gestion_observaciones")
    public String listarAbogados(@RequestParam(defaultValue = "0") int pagina,
                                 @RequestParam(defaultValue = "10") int tamano,
                                 Model model, 
                                 HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Page<Observacion> paginaAbogados = observacionServicio.listarPaginados(pagina, tamano);

        model.addAttribute("observaciones", paginaAbogados.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaAbogados.getTotalPages());
        model.addAttribute("totalElementos", paginaAbogados.getTotalElements());
        model.addAttribute("usuarioActual", usuario);

        return "admin/gestion_observaciones";
    }

    @DeleteMapping("/eliminar_observacion/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = observacionServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    } 

    @PostMapping("/admin/guardar_observacion")
    public String guardarPermiso(@ModelAttribute Observacion dato, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        LocalDateTime fechaActual = LocalDateTime.now();
        dato.setCreadoPor(usuarioSesion);
        dato.setFechaCreacion(fechaActual);
        observacionServicio.guardar(dato);
        return "redirect:/admin/gestion_permisos";
    }
}
