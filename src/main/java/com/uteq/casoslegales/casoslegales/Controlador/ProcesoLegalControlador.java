package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.AudienciaEvento;
import com.uteq.casoslegales.casoslegales.Modelo.Chat;
import com.uteq.casoslegales.casoslegales.Modelo.ChatMensaje;
import com.uteq.casoslegales.casoslegales.Modelo.Documento;
import com.uteq.casoslegales.casoslegales.Modelo.EstadoProceso;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoUsuario;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.AudienciaEventoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ChatMensajeServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ChatServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ClienteServicio;
import com.uteq.casoslegales.casoslegales.Servicio.DocumentoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.EstadoProcesoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoLegalServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoUsuarioServicio;
import com.uteq.casoslegales.casoslegales.Servicio.UsuarioServicio;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ProcesoLegalControlador {

    @Autowired
    private ProcesoLegalServicio procesoServicio;

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private EstadoProcesoServicio estadoProcesoServicio;

    @Autowired
    private ProcesoUsuarioServicio procesoUsuarioServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ChatServicio chatServicio;

    @Autowired
    private ChatMensajeServicio chatMensajeServicio;

    @Autowired
    private AudienciaEventoServicio audienciaEventoServicio;

    @Autowired
    private DocumentoServicio documentoServicio;

    private static final Logger logger = LoggerFactory.getLogger(ProcesoLegalControlador.class);

    @GetMapping("/admin/gestion_procesos_legales")
    public String listarProcesosLegales(@RequestParam(defaultValue = "0") int pagina,
                                        @RequestParam(defaultValue = "10") int tamano,
                                        Model model,
                                        HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !"admin".equalsIgnoreCase(usuario.getRol().getNombre())) {
            return "redirect:/login";
        }
        Page<ProcesoLegal> paginaProcesos = procesoServicio.listarPaginados(pagina, tamano);
        model.addAttribute("procesos", paginaProcesos.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaProcesos.getTotalPages());
        model.addAttribute("totalElementos", paginaProcesos.getTotalElements());
        return "admin/gestion_procesos_legales";
    }

    @DeleteMapping("/eliminar_proceso/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(id, usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            boolean eliminado = procesoServicio.eliminarPorId(id, usuario);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar: " + e.getMessage());
        }
    }

   @GetMapping({"/abogado/inicio", "/cliente/inicio"})
    public String mostrarMisCasos(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        List<ProcesoLegal> procesos = procesoServicio.listarPorUsuario(usuario.getId());
        model.addAttribute("procesos", procesos);
        // Devolver la vista correspondiente según el rol
        return "abogado".equalsIgnoreCase(usuario.getRol().getNombre()) ? 
               "abogado/inicio" : "cliente/inicio";
    }

    @GetMapping({"/abogado/inicio_agregar", "/admin/gestion_procesos_legales_agregar"})
    public String mostrarFormularioNuevoCaso(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("procesoLegal", new ProcesoLegal());
        model.addAttribute("clientes", clienteServicio.listarTodos());
        model.addAttribute("estados", estadoProcesoServicio.listarTodos());
        model.addAttribute("usuarios", usuarioServicio.listarTodos());
        return "admin".equalsIgnoreCase(usuario.getRol().getNombre()) ?
               "admin/gestion_procesos_legales_agregar" : "abogado/inicio_agregar";
    }

    @GetMapping("/listar_procesos_legales")
    @ResponseBody
    public List<Object[]> listarProcesos(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return List.of();
        }
        return procesoServicio.listarProcesoLegalMinimo();
    }

    @PostMapping({"/abogado/guardar_proceso_legal", "/admin/guardar_proceso_legal"})
    public String guardarProcesoLegal(
            @ModelAttribute ProcesoLegal procesoLegal,
            @RequestParam(value = "abogadosSeleccionados", required = false) List<Long> abogadosSeleccionados,
            @RequestParam(value = "principal", required = false) List<Long> principales,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        try {
            LocalDateTime fechaActual = LocalDateTime.now();
            procesoLegal.setCreadoPor(usuario);
            procesoLegal.setFechaCreacion(fechaActual);
            procesoLegal = procesoServicio.guardar(procesoLegal);

            if (abogadosSeleccionados != null) {
                for (Long abogadoId : abogadosSeleccionados) {
                    if (abogadoId == null) continue;
                    Usuario abogado = usuarioServicio.buscarPorId(abogadoId)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + abogadoId));
                    ProcesoUsuario pu = new ProcesoUsuario();
                    pu.setProceso(procesoLegal);
                    pu.setUsuario(abogado);
                    pu.setEsResponsablePrincipal(principales != null && principales.contains(abogadoId));
                    pu.setCreadoPor(usuario);
                    pu.setFechaCreacion(fechaActual);
                    procesoUsuarioServicio.guardar(pu);
                }
            }
            redirectAttributes.addFlashAttribute("exito", true);
            return "admin".equalsIgnoreCase(usuario.getRol().getNombre()) ?
                   "redirect:/admin/gestion_procesos_legales_agregar" : "redirect:/abogado/inicio_agregar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            return "admin".equalsIgnoreCase(usuario.getRol().getNombre()) ?
                   "redirect:/admin/gestion_procesos_legales_agregar" : "redirect:/abogado/inicio_agregar";
        }
    }

    @GetMapping({"/abogado/proceso_legal_contenido/{id}", "/cliente/proceso_legal_contenido/{id}"})
    public String mostrarDetalleCaso(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Optional<ProcesoLegal> procesoOpt = procesoServicio.obtenerPorId(id);
        if (procesoOpt.isEmpty()) {
            return "abogado".equalsIgnoreCase(usuario.getRol().getNombre()) ? 
                   "redirect:/abogado/inicio" : "redirect:/cliente/inicio";
        }

        ProcesoLegal proceso = procesoOpt.get();
        // Verificar si el usuario es admin, está involucrado, o es el cliente del proceso
        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(proceso.getId(), usuario.getId()) &&
            !(proceso.getCliente() != null && proceso.getCliente().getUsuario().getId().equals(usuario.getId()))) {
            return "abogado".equalsIgnoreCase(usuario.getRol().getNombre()) ? 
                   "redirect:/abogado/inicio" : "redirect:/cliente/inicio";
        }

        session.setAttribute("idProceso", id);
        Optional<Chat> chatOpt = chatServicio.obtenerPorProcesoId(proceso.getId());
        Chat chat = chatOpt.orElseGet(() -> crearNuevoChat(proceso, usuario));
        List<ChatMensaje> mensajes = chatMensajeServicio.obtenerMensajesPorChat(chat.getId());
        List<ProcesoUsuario> usuariosInvolucrados = procesoUsuarioServicio.listarPorProcesoId(proceso.getId());
        List<AudienciaEvento> eventos = audienciaEventoServicio.listarPorProcesoId(proceso.getId());
        Map<String, List<Documento>> documentosAgrupados = documentoServicio.obtenerAgrupadosPorFecha(proceso.getId());
        List<Usuario> usuariosDisponibles = usuarioServicio.listarNoInvolucradosEnProceso(proceso.getId());
        List<EstadoProceso> estadosDisponibles = estadoProcesoServicio.listarTodos();

        model.addAttribute("proceso", proceso);
        model.addAttribute("mensajes", mensajes);
        model.addAttribute("usuariosInvolucrados", usuariosInvolucrados);
        model.addAttribute("eventos", eventos);
        model.addAttribute("documentosAgrupados", documentosAgrupados);
        model.addAttribute("usuarioActual", usuario);
        model.addAttribute("now", LocalDateTime.now());
        model.addAttribute("usuariosDisponibles", usuariosDisponibles);
        model.addAttribute("estadosDisponibles", estadosDisponibles);

        return "abogado".equalsIgnoreCase(usuario.getRol().getNombre()) ? 
               "abogado/proceso_legal_contenido" : "cliente/proceso_legal_contenido";
    }


    private Chat crearNuevoChat(ProcesoLegal proceso, Usuario usuario) {
        Chat chat = new Chat();
        chat.setTitulo(proceso.getNumeroProceso());
        chat.setProceso(proceso);
        chat.setCreadoPor(usuario);
        chat.setFechaCreacion(LocalDateTime.now());
        return chatServicio.guardar(chat);
    }

    @GetMapping("/abogado/eventos-proceso/{id}")
    @ResponseBody
    public ResponseEntity<?> listarEventosPorProceso(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            logger.warn("No hay usuario en la sesión para /abogado/eventos-proceso/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(id, usuario.getId())) {
            logger.warn("Usuario {} no tiene acceso al proceso con ID: {}", usuario.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }

        try {
            List<AudienciaEvento> eventos = audienciaEventoServicio.listarPorProcesoId(id);
            logger.info("Eventos obtenidos para el proceso {}: {}", id, eventos.size());

            // Mapear a estructura simple para evitar problemas de serialización
            List<Map<String, Object>> eventosSimples = eventos.stream().map(evento -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", evento.getId());
                map.put("tipoEvento", evento.getTipoEvento());
                map.put("descripcion", evento.getDescripcion());
                map.put("fechaEvento", evento.getFechaEvento());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(eventosSimples);
        } catch (Exception e) {
            logger.error("Error al listar eventos para el proceso {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los eventos");
        }
    }

    @GetMapping("/abogado/documentos-proceso/{id}")
    @ResponseBody
    public ResponseEntity<?> listarDocumentosPorProceso(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            logger.warn("No hay usuario en la sesión para /abogado/documentos-proceso/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(id, usuario.getId())) {
            logger.warn("Usuario {} no tiene acceso al proceso con ID: {}", usuario.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }

        try {
            List<Documento> documentos = documentoServicio.listarPorProcesoId(id);
            logger.info("Documentos obtenidos para el proceso {}: {}", id, documentos.size());

            // Mapear a estructura simple
            List<Map<String, Object>> documentosSimples = documentos.stream().map(doc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", doc.getId());
                map.put("nombre", doc.getNombre());
                map.put("tipoDocumento", doc.getTipoDocumento());
                map.put("rutaArchivo", doc.getRutaArchivo());
                map.put("fechaCreacion", doc.getFechaCreacion());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(documentosSimples);
        } catch (Exception e) {
            logger.error("Error al listar documentos para el proceso {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los documentos");
        }
    }

    @PostMapping("/abogado/actualizar-proceso/{id}")
    public ResponseEntity<?> actualizarProcesoParcial(@PathVariable Long id,
                                                     @RequestParam String field,
                                                     @RequestParam String value,
                                                     HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(id, usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        Optional<ProcesoLegal> procesoOpt = procesoServicio.obtenerPorId(id);
        if (procesoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proceso no encontrado");
        }
        ProcesoLegal proceso = procesoOpt.get();
        try {
            switch (field) {
                case "numeroProceso":
                    proceso.setNumeroProceso(value);
                    break;
                case "descripcion":
                    proceso.setDescripcion(value.isEmpty() ? null : value);
                    break;
                case "estado":
                    Optional<EstadoProceso> estadoOpt = estadoProcesoServicio.buscarPorNombre(value);
                    if (estadoOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body("Estado no encontrado");
                    }
                    proceso.setEstado(estadoOpt.get());
                    break;
                case "fechaInicio":
                    proceso.setFechaInicio(LocalDate.parse(value).atStartOfDay());
                    break;
                default:
                    return ResponseEntity.badRequest().body("Campo inválido");
            }
            proceso.setModificadoPor(usuario);
            proceso.setFechaModificacion(LocalDateTime.now());
            procesoServicio.guardar(proceso);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar: " + e.getMessage());
        }
    }

    @PostMapping("/abogado/agregar-usuario-proceso/{id}")
    public ResponseEntity<?> agregarUsuarioProceso(@PathVariable Long id,
                                                  @RequestParam Long usuarioId,
                                                  @RequestParam(defaultValue = "false") boolean esResponsablePrincipal,
                                                  HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        if (!"admin".equalsIgnoreCase(usuarioSesion.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(id, usuarioSesion.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        Optional<ProcesoLegal> procesoOpt = procesoServicio.obtenerPorId(id);
        if (procesoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proceso no encontrado");
        }
        Optional<Usuario> usuarioOpt = usuarioServicio.buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        try {
            ProcesoUsuario pu = new ProcesoUsuario();
            pu.setProceso(procesoOpt.get());
            pu.setUsuario(usuarioOpt.get());
            pu.setEsResponsablePrincipal(esResponsablePrincipal);
            pu.setCreadoPor(usuarioSesion);
            pu.setFechaCreacion(LocalDateTime.now());
            procesoUsuarioServicio.guardar(pu);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al agregar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/abogado/eliminar-usuario-proceso/{procesoId}/{usuarioId}")
    public ResponseEntity<?> eliminarUsuarioProceso(@PathVariable Long procesoId, @PathVariable Long usuarioId, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        if (!"admin".equalsIgnoreCase(usuarioSesion.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(procesoId, usuarioSesion.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            procesoUsuarioServicio.eliminarPorProcesoYUsuario(procesoId, usuarioId, usuarioSesion);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar usuario del proceso: " + e.getMessage());
        }
    }

    @PostMapping("/abogado/guardar-evento2")
    public ResponseEntity<?> guardarEvento(@ModelAttribute AudienciaEvento evento,
                                          HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        Optional<ProcesoLegal> procesoOpt = procesoServicio.obtenerPorId(evento.getProceso().getId());
        if (procesoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proceso no encontrado");
        }
        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(evento.getProceso().getId(), usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            if (evento.getId() == null) {
                evento.setCreadoPor(usuario);
                evento.setFechaCreacion(LocalDateTime.now());
            } else {
                Optional<AudienciaEvento> existingEvento = audienciaEventoServicio.obtenerPorId(evento.getId());
                if (existingEvento.isPresent()) {
                    AudienciaEvento updatedEvento = existingEvento.get();
                    updatedEvento.setTipoEvento(evento.getTipoEvento());
                    updatedEvento.setDescripcion(evento.getDescripcion());
                    updatedEvento.setFechaEvento(evento.getFechaEvento());
                    updatedEvento.setModificadoPor(usuario);
                    updatedEvento.setFechaModificacion(LocalDateTime.now());
                    audienciaEventoServicio.guardar(updatedEvento);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Evento no encontrado");
                }
            }
            audienciaEventoServicio.guardar(evento);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar evento: " + e.getMessage());
        }
    }

    @DeleteMapping("/abogado/eliminar-evento/{id}")
    public ResponseEntity<?> eliminarEvento(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        Optional<AudienciaEvento> eventoOpt = audienciaEventoServicio.obtenerPorId(id);
        if (eventoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Evento no encontrado");
        }
        AudienciaEvento evento = eventoOpt.get();
        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(evento.getProceso().getId(), usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            audienciaEventoServicio.eliminarPorId(id, usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar evento: " + e.getMessage());
        }
    }

    @PostMapping("/abogado/subir-documento/{id}")
    public ResponseEntity<?> subirDocumento(@PathVariable Long id,
                                           @RequestParam("archivo") MultipartFile archivo,
                                           @RequestParam String nombre,
                                           @RequestParam(required = false) String tipoDocumento,
                                           HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(id, usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        Optional<ProcesoLegal> procesoOpt = procesoServicio.obtenerPorId(id);
        if (procesoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proceso no encontrado");
        }
        try {
            String ruta = documentoServicio.guardarArchivo(archivo);
            Documento doc = new Documento();
            doc.setProceso(procesoOpt.get());
            doc.setNombre(nombre.isEmpty() ? archivo.getOriginalFilename() : nombre);
            doc.setRutaArchivo(ruta);
            doc.setTipoDocumento(tipoDocumento);
            doc.setPalabrasClave("clave edad");
            doc.setClaveCifrado("20");
            LocalDateTime now = LocalDateTime.now();
            doc.setAnio(now.getYear());
            doc.setMes(now.getMonthValue());
            doc.setCreadoPor(usuario);
            doc.setFechaCreacion(now);
            documentoServicio.guardar(doc);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir documento: " + e.getMessage());
        }
    }

    @PostMapping("/abogado/actualizar-documento/{id}")
    public ResponseEntity<?> actualizarDocumento(@PathVariable Long id,
                                                 @RequestParam(required = false) MultipartFile archivo,
                                                 @RequestParam String nombre,
                                                 @RequestParam(required = false) String tipoDocumento,
                                                 HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        Optional<Documento> docOpt = documentoServicio.obtenerPorId(id);
        if (docOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
        Documento doc = docOpt.get();
        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(doc.getProceso().getId(), usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            if (archivo != null && !archivo.isEmpty()) {
                String ruta = documentoServicio.guardarArchivo(archivo);
                doc.setRutaArchivo(ruta);
            }
            doc.setNombre(nombre);
            doc.setTipoDocumento(tipoDocumento);
            doc.setModificadoPor(usuario);
            doc.setFechaModificacion(LocalDateTime.now());
            documentoServicio.guardar(doc);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar documento: " + e.getMessage());
        }
    }

    @DeleteMapping("/abogado/eliminar-documento/{id}")
    public ResponseEntity<?> eliminarDocumento(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        Optional<Documento> docOpt = documentoServicio.obtenerPorId(id);
        if (docOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        }
        Documento doc = docOpt.get();
        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(doc.getProceso().getId(), usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        try {
            documentoServicio.eliminarPorId(id, usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar documento: " + e.getMessage());
        }
    }
}