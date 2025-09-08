package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.Cliente;
import com.uteq.casoslegales.casoslegales.Modelo.Rol;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.ClienteServicio;
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
public class ClienteControlador {

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private RolServicio rolServicio;

    @GetMapping("/admin/gestion_clientes")
    public String listarClientes(@RequestParam(defaultValue = "0") int pagina,
                                 @RequestParam(defaultValue = "10") int tamano,
                                 Model model) {
        Page<Cliente> paginaClientes = clienteServicio.listarPaginados(pagina, tamano);

        model.addAttribute("clientes", paginaClientes.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaClientes.getTotalPages());
        model.addAttribute("totalElementos", paginaClientes.getTotalElements());

        return "admin/gestion_clientes";
    }

    @DeleteMapping("/eliminar_cliente/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = clienteServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping({"/admin/guardar_cliente"})
    public String guardarCliente(
            @ModelAttribute Usuario usuario,
            @RequestParam("direccion") String direccion,
            @RequestParam("confirmarContrasenia") String confirmarContrasenia,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        try {
            if (!usuario.getContrasenia().equals(confirmarContrasenia)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/admin/gestion_clientes_agregar";
            }

            LocalDateTime fechaActual = LocalDateTime.now();

            String contraseniaEncriptada = passwordEncoder.encode(usuario.getContrasenia());
            usuario.setContrasenia(contraseniaEncriptada);

            usuario.setCreadoPor(usuarioSesion);
            usuario.setFechaCreacion(fechaActual);

            Rol rolCliente = rolServicio.obtenerPorNombre("Cliente");
            usuario.setRol(rolCliente);

            usuario = usuarioServicio.guardar(usuario);

            Cliente cliente = new Cliente();
            cliente.setDireccion(direccion);
            cliente.setCreadoPor(usuarioSesion);
            cliente.setUsuario(usuario);
            cliente.setFechaCreacion(fechaActual);

            clienteServicio.guardar(cliente);

            redirectAttributes.addFlashAttribute("exito", true);
            return "redirect:/admin/gestion_clientes_agregar";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario: " + e.getMessage());
            return "redirect:/admin/gestion_clientes_agregar";
        }
    }
}
