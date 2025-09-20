package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.Permiso;
import com.uteq.casoslegales.casoslegales.Modelo.Rol;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.PermisoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.RolServicio;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PermisoControlador {

    @Autowired
    private PermisoServicio permisoServicio;

    @Autowired
    private RolServicio rolServicio;

    @GetMapping("/admin/gestion_permisos")
    public String listarPermisos(@RequestParam(defaultValue = "0") int pagina,
                                 @RequestParam(defaultValue = "10") int tamano,
                                 Model model, 
                                 HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Page<Permiso> paginaPermisos = permisoServicio.listarPaginados(pagina, tamano);
        model.addAttribute("permisos", paginaPermisos.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaPermisos.getTotalPages());
        model.addAttribute("totalElementos", paginaPermisos.getTotalElements());
        model.addAttribute("usuarioActual", usuario);

        return "admin/gestion_permisos";
    }

    @DeleteMapping("/eliminar_permiso/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = permisoServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/listar_roles")
    @ResponseBody
    public List<Rol> listarRoles() {
        return rolServicio.listarTodos();
    }

    @PostMapping("/admin/guardar_permiso")
    public String guardarPermiso(@ModelAttribute Permiso permiso, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        LocalDateTime fechaActual = LocalDateTime.now();
        permiso.setCreadoPor(usuarioSesion);
        permiso.setFechaCreacion(fechaActual);
        permisoServicio.guardar(permiso);
        return "redirect:/admin/gestion_permisos";
    }
}