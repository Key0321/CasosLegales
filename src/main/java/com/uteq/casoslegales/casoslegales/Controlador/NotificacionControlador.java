package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.Notificacion;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.NotificacionServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoLegalServicio;
import com.uteq.casoslegales.casoslegales.Servicio.UsuarioServicio;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NotificacionControlador {

    @Autowired
    private NotificacionServicio notificacionServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ProcesoLegalServicio procesoServicio;

   @GetMapping("/admin/gestion_notificaciones")
    public String listarAbogados(@RequestParam(defaultValue = "0") int pagina,
                                 @RequestParam(defaultValue = "10") int tamano,
                                 Model model,
                                 HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Page<Notificacion> paginaAbogados = notificacionServicio.listarPaginados(pagina, tamano);

        model.addAttribute("notificaciones", paginaAbogados.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaAbogados.getTotalPages());
        model.addAttribute("totalElementos", paginaAbogados.getTotalElements());

        return "admin/gestion_notificaciones";
    }

    @DeleteMapping("/eliminar_notificacion/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = notificacionServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping({"/abogado/inicio_agregar_notificacion", "/admin/gestion_notificaciones_agregar"})
    public String mostrarFormularioNuevaNotificacion(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setEnviadoCorreo(false); // Valor por defecto
        model.addAttribute("notificacion", notificacion);

        List<Object[]> procesosMinimos = procesoServicio.listarProcesoLegalMinimo();
        List<Object[]> usuarioMinimos = usuarioServicio.listarUsuarioMinimo();
        
        model.addAttribute("usuarios", usuarioMinimos);
        model.addAttribute("procesos", procesosMinimos);

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if ("admin".equals(usuarioSesion.getRol().getNombre().toLowerCase())) {
            return "admin/gestion_notificaciones_agregar";
        } else {
            return "abogado/inicio_agregar_notificacion";
        }
    }

    @PostMapping({"/admin/guardar_notificacion"})
    public String guardarNotificacion(
            @ModelAttribute Notificacion notificacion,
            @RequestParam(value = "enviadoCorreo", required = false) Boolean enviadoCorreo,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        try {
            LocalDateTime fechaActual = LocalDateTime.now();

            // IMPORTANTE: Usar el valor del checkbox, no establecer siempre false
            notificacion.setEnviadoCorreo(Boolean.TRUE.equals(enviadoCorreo));
            
            notificacion.setCreadoPor(usuarioSesion);
            notificacion.setFechaCreacion(fechaActual);
            notificacion.setFechaEnvio(fechaActual);

            notificacion = notificacionServicio.guardar(notificacion);

            redirectAttributes.addFlashAttribute("exito", true);
            return "redirect:/admin/gestion_notificaciones_agregar";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar la notificación: " + e.getMessage());
            return "redirect:/admin/gestion_notificaciones_agregar";
        }
    }
}
