package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.Rol;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.RolServicio;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class RolControlador {

    @Autowired
    private RolServicio rolServicio;

    @GetMapping("/admin/gestion_roles")
    public String listarRoles(@RequestParam(defaultValue = "0") int pagina,
                              @RequestParam(defaultValue = "10") int tamano,
                              Model model, 
                                 HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Page<Rol> paginaRoles = rolServicio.listarPaginados(pagina, tamano);

        model.addAttribute("roles", paginaRoles.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaRoles.getTotalPages());
        model.addAttribute("totalElementos", paginaRoles.getTotalElements());
        model.addAttribute("usuarioActual", usuario);

        return "admin/gestion_roles";
    }

    @DeleteMapping("/eliminar_rol/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = rolServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/guardar_rol")
    public String guardarEstado(@ModelAttribute Rol dato) {
        dato = rolServicio.guardar(dato);
        return "redirect:/admin/gestion_roles";
    }
}
