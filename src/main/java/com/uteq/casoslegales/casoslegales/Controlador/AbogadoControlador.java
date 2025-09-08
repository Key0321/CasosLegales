package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.Abogado;
import com.uteq.casoslegales.casoslegales.Modelo.Rol;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.AbogadoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.RolServicio;
import com.uteq.casoslegales.casoslegales.Servicio.UsuarioServicio;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AbogadoControlador {

    @Autowired
    private AbogadoServicio abogadoServicio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private RolServicio rolServicio;

    @GetMapping("/admin/gestion_abogados")
    public String listarAbogados(@RequestParam(defaultValue = "0") int pagina,
                                 @RequestParam(defaultValue = "10") int tamano,
                                 Model model) {
        Page<Abogado> paginaAbogados = abogadoServicio.listarPaginados(pagina, tamano);

        model.addAttribute("abogados", paginaAbogados.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaAbogados.getTotalPages());
        model.addAttribute("totalElementos", paginaAbogados.getTotalElements());

        return "admin/gestion_abogados";
    }

    @DeleteMapping("/eliminar_abogado/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = abogadoServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

   @PostMapping({"/admin/guardar_abogado"})
    public String guardarCliente(
            @ModelAttribute Usuario usuario,
            @RequestParam("especialidad") String especialidad,
            @RequestParam("confirmarContrasenia") String confirmarContrasenia,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        try {
            if (!usuario.getContrasenia().equals(confirmarContrasenia)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/admin/gestion_abogados_agregar";
            }

            LocalDateTime fechaActual = LocalDateTime.now();

            String contraseniaEncriptada = passwordEncoder.encode(usuario.getContrasenia());
            usuario.setContrasenia(contraseniaEncriptada);

            usuario.setCreadoPor(usuarioSesion);
            usuario.setFechaCreacion(fechaActual);

            Rol rolCliente = rolServicio.obtenerPorNombre("Abogado");
            usuario.setRol(rolCliente);

            usuario = usuarioServicio.guardar(usuario);

            Abogado abogado = new Abogado();
            abogado.setEspecialidad(especialidad);
            abogado.setCreadoPor(usuarioSesion);
            abogado.setUsuario(usuario);
            abogado.setFechaCreacion(fechaActual);

            abogadoServicio.guardar(abogado);

            redirectAttributes.addFlashAttribute("exito", true);
            return "redirect:/admin/gestion_abogados_agregar";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario: " + e.getMessage());
            return "redirect:/admin/gestion_abogados_agregar";
        }
    }
}
