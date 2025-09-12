package com.uteq.casoslegales.casoslegales.Controlador;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uteq.casoslegales.casoslegales.Modelo.Acceso;
import com.uteq.casoslegales.casoslegales.Modelo.Rol;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.AccesoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.RolServicio;
import com.uteq.casoslegales.casoslegales.Servicio.UsuarioServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolServicio rolServicio;

    @Autowired
    private AccesoServicio accesoServicio;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/admin/gestion_usuarios")
    public String listarUsuarios(@RequestParam(defaultValue = "0") int pagina,
                                @RequestParam(defaultValue = "10") int tamano,
                                Model model) {

        Page<Usuario> paginaUsuarios = usuarioServicio.listarPaginados(pagina, tamano);

        List<Usuario> usuarios = paginaUsuarios.getContent();
        Map<Long, Acceso> ultimoAccesoMap = new HashMap<>();

        for (Usuario u : usuarios) {
            Acceso ultimo = accesoServicio.obtenerUltimoAcceso(u);
            ultimoAccesoMap.put(u.getId(), ultimo);
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("ultimoAccesoMap", ultimoAccesoMap);
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaUsuarios.getTotalPages());
        model.addAttribute("totalElementos", paginaUsuarios.getTotalElements());

        return "admin/gestion_usuarios";
    }

    @GetMapping("/abogado/usuarios")
    public String listarUsuarios(@RequestParam(defaultValue = "0") int pagina,
                                @RequestParam(defaultValue = "10") int tamano,
                                @RequestParam(required = false) String busqueda,
                                HttpSession session, 
                                Model model) {

        // Obtener usuario de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().getNombre().equals("Abogado")) {
            return "redirect:/login";
        }

        // Listar usuarios con paginación y filtro de búsqueda
        Page<Usuario> paginaUsuarios = usuarioServicio.listarPaginadosConFiltro(busqueda, usuario.getId(), pagina, tamano);

        // Obtener últimos accesos
        List<Usuario> usuarios = paginaUsuarios.getContent();
        Map<Long, Acceso> ultimoAccesoMap = new HashMap<>();
        for (Usuario u : usuarios) {
            Acceso ultimo = accesoServicio.obtenerUltimoAcceso(u);
            ultimoAccesoMap.put(u.getId(), ultimo);
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("ultimoAccesoMap", ultimoAccesoMap);
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaUsuarios.getTotalPages());
        model.addAttribute("totalElementos", paginaUsuarios.getTotalElements());
        model.addAttribute("filtroBusqueda", busqueda);
        model.addAttribute("usuarioActual", usuario);

        return "abogado/usuarios";
    }

    @PostMapping("/cerrar_sesion_usuario/{id}")
    public ResponseEntity<?> cerrarSesionUsuario(@PathVariable Long id) {
        try {
            Acceso ultimoAcceso = accesoServicio.obtenerUltimoAccesoPorUsuarioId(id);
            if (ultimoAcceso != null && ultimoAcceso.getFechaSalida() == null) {
                accesoServicio.registrarSalida(ultimoAcceso.getId());
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("No hay una sesión activa para este usuario");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cerrar la sesión: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar_usuario/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = usuarioServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping({
        "/abogado/usuarios_agregar",
        "/admin/gestion_usuarios_agregar",
        "/admin/gestion_clientes_agregar",
        "/admin/gestion_abogados_agregar"
    })
    public String mostrarFormularioNuevoUsuario(HttpSession session, Model model, HttpServletRequest request) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolServicio.listarTodos());

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        String uri = request.getRequestURI();

        if ("admin".equals(usuarioSesion.getRol().getNombre().toLowerCase())) {
            if (uri.contains("gestion_clientes")) {
                return "admin/gestion_clientes_agregar";
            } else if (uri.contains("gestion_abogados")) {
                return "admin/gestion_abogados_agregar";
            } else {
                return "admin/gestion_usuarios_agregar";
            }
        } else {
            return "abogado/usuarios_agregar";
        }
    }

    @PostMapping({"/admin/guardar_usuario"})
     public String guardarUsuario(
            @ModelAttribute Usuario usuario,
            @RequestParam("confirmarContrasenia") String confirmarContrasenia,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        try {
            // Validar que las contraseñas coincidan
            if (!usuario.getContrasenia().equals(confirmarContrasenia)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/admin/gestion_usuarios_agregar";
            }

            LocalDateTime fechaActual = LocalDateTime.now();

            // Encriptar la contraseña antes de guardarla
            String contraseniaEncriptada = passwordEncoder.encode(usuario.getContrasenia());
            usuario.setContrasenia(contraseniaEncriptada);

            usuario.setCreadoPor(usuarioSesion);
            usuario.setFechaCreacion(fechaActual);

            usuario = usuarioServicio.guardar(usuario);

            // Obtener nombre del rol para asignar permisos en BD
            Rol rol = rolServicio.obtenerPorId(usuario.getRol().getId())
                    .orElseThrow(() -> new Exception("Rol no encontrado"));
            
            // Crear usuario en PostgreSQL con permisos según rol
            jdbcTemplate.execute("CALL crear_usuario_db('" + usuario.getCorreo() + "', '" + usuario.getContrasenia() + "', '" + rol.getNombre() + "')");

            redirectAttributes.addFlashAttribute("exito", true);
            return "redirect:/admin/gestion_usuarios_agregar";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario: " + e.getMessage());
            return "redirect:/admin/gestion_usuarios_agregar";
        }
    }

    @GetMapping("/listar_usuarios")
    @ResponseBody
    public List<Object[]> listarUsuarios() {
        return usuarioServicio.listarUsuarioMinimo();
    }

    // Mostrar formulario para crear nuevo usuario
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/formulario"; 
    }

    // Guardar usuario (nuevo o editado)
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario) {
        usuarioServicio.guardar(usuario);
        return "redirect:/usuarios";
    }

    // Mostrar formulario para editar usuario
    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Optional<Usuario> usuario = usuarioServicio.buscarPorId(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "usuarios/formulario"; // Reutiliza el mismo formulario
        } else {
            return "redirect:/usuarios"; // O muestra un error
        }
    }

    // Eliminar usuario
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioServicio.eliminar(id);
        return "redirect:/usuarios";
    }
}
