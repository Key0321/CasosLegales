package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.EstadoProceso;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.EstadoProcesoServicio;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class EstadoProcesoControlador {

    @Autowired
    private EstadoProcesoServicio estadoServicio;

    @GetMapping("/admin/gestion_estados")
    public String listarEstadosProcesos(@RequestParam(defaultValue = "0") int pagina,
                                        @RequestParam(defaultValue = "10") int tamano,
                                        Model model,
                                 HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Page<EstadoProceso> paginaEstados = estadoServicio.listarPaginados(pagina, tamano);

        model.addAttribute("estados", paginaEstados.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaEstados.getTotalPages());
        model.addAttribute("totalElementos", paginaEstados.getTotalElements());

        return "admin/gestion_estados";
    }

    @PostMapping("/admin/guardar_estado")
    public String guardarEstado(@ModelAttribute EstadoProceso estadoProceso) {
        estadoProceso = estadoServicio.guardar(estadoProceso);
        return "redirect:/admin/gestion_estados";
    }
    
    @DeleteMapping("/eliminar_estado/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = estadoServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

