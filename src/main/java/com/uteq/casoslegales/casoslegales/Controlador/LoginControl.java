package com.uteq.casoslegales.casoslegales.Controlador;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uteq.casoslegales.casoslegales.Modelo.Acceso;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.AccesoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.UsuarioServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginControl {

    @Autowired
    private UsuarioServicio usuarioService;

    @Autowired
    private AccesoServicio accesoService;

    @GetMapping("/login")
    public String mostrarFormularioLogin() {
        return "login"; // login.html
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                @RequestParam String contrasenia,
                                HttpSession session,
                                Model model,
                                HttpServletRequest request) {
        Optional<Usuario> usuario = usuarioService.autenticar(correo, contrasenia);
        if (usuario.isPresent()) {
            Usuario user = usuario.get();

            session.setAttribute("usuario", user);
            session.setAttribute("rol", user.getRol().getNombre());

            Acceso acceso = new Acceso();
            acceso.setUsuario(user);
            acceso.setFechaIngreso(LocalDateTime.now());
            acceso.setIpOrigen(request.getRemoteAddr());
            acceso.setCreadoPor(user); // o un admin, depende de la lógica
            acceso.setFechaCreacion(LocalDateTime.now());
            accesoService.guardar(acceso);

            session.setAttribute("accesoId", acceso.getId());


            switch (user.getRol().getNombre().toLowerCase()) {
                case "abogado":
                    return "redirect:/abogado/inicio";
                case "cliente":
                    return "redirect:/cliente/inicio";
                case "admin":
                    return "redirect:/admin/gestion_procesos_legales";
                default:
                    model.addAttribute("error", "Rol no reconocido");
                    return "login";
            }
        } else {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        Long accesoId = (Long) session.getAttribute("accesoId");

        if (accesoId != null) {
            accesoService.registrarSalida(accesoId); // marca fecha_salida
        }

        session.invalidate();
        return "redirect:/login";
    }

}
