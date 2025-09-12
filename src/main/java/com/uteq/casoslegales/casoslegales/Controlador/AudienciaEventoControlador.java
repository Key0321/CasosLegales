package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.AudienciaEvento;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoUsuario;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.AudienciaEventoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoLegalServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoUsuarioServicio;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;


@Controller
public class AudienciaEventoControlador {

    private static final Logger logger = LoggerFactory.getLogger(AudienciaEventoControlador.class);

    @Autowired
    private AudienciaEventoServicio audienciaServicio;

    @Autowired
    private ProcesoLegalServicio procesoServicio;

    @Autowired
    private ProcesoUsuarioServicio procesoUsuarioServicio;

    @GetMapping("/admin/gestion_eventos")
    public String listarAudienciasEventos(@RequestParam(defaultValue = "0") int pagina,
                                          @RequestParam(defaultValue = "10") int tamano,
                                          Model model) {
        Page<AudienciaEvento> paginaAudiencias = audienciaServicio.listarPaginados(pagina, tamano);

        model.addAttribute("audiencias", paginaAudiencias.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaAudiencias.getTotalPages());
        model.addAttribute("totalElementos", paginaAudiencias.getTotalElements());

        return "admin/gestion_eventos";
    }
    

    @DeleteMapping("/eliminar_audiencia/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = audienciaServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/Audiencia/listar_procesos_legales")
    @ResponseBody
    public List<Object[]> listarProcesos() {
        return procesoServicio.listarProcesoLegalMinimo();
    }

    @GetMapping("/Audiencia/listar_mis_procesos_legales")
    @ResponseBody
    public ResponseEntity<List<Object[]>> listarMisProcesos(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        try {
            List<ProcesoLegal> procesos = procesoServicio.listarPorUsuario(usuario.getId());
            List<Object[]> procesosMinimos = procesos.stream()
                    .filter(p -> p.getFechaEliminacion() == null)
                    .map(p -> new Object[]{
                        p.getId(),
                        p.getNumeroProceso(),
                        p.getCliente() != null && p.getCliente().getUsuario() != null
                            ? p.getCliente().getUsuario().getNombre() + " " + p.getCliente().getUsuario().getApellido()
                            : "Cliente desconocido"
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(procesosMinimos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PostMapping("/admin/guardar_audiencia_evento")
    public String guardarPermiso(@ModelAttribute AudienciaEvento dato, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        LocalDateTime fechaActual = LocalDateTime.now();
        dato.setCreadoPor(usuarioSesion);
        dato.setFechaCreacion(fechaActual);
        audienciaServicio.guardar(dato);
        return "redirect:/admin/gestion_permisos";
    }

    @GetMapping({"/abogado/calendario", "/cliente/calendario"})
    public String mostrarCalendario(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            logger.warn("No hay usuario en la sesión, redirigiendo a login");
            return "redirect:/login";
        }

        logger.info("Cargando calendario para usuario ID: {}", usuario.getId());

        // Obtener procesos en los que el usuario está involucrado
        List<ProcesoUsuario> procesosInvolucrados = procesoUsuarioServicio.listarPorUsuarioId(usuario.getId());
        logger.info("Procesos involucrados encontrados: {}", procesosInvolucrados.size());

        List<AudienciaEvento> eventos = new ArrayList<>();
        if (!procesosInvolucrados.isEmpty()) {
            // Obtener IDs de procesos
            List<Long> procesoIds = procesosInvolucrados.stream()
                    .map(ProcesoUsuario::getProceso)
                    .map(ProcesoLegal::getId)
                    .collect(Collectors.toList());
            logger.info("IDs de procesos: {}", procesoIds);

            // Obtener todos los eventos y audiencias asociadas a los procesos del usuario
            eventos = audienciaServicio.listarPorProcesoIds(procesoIds);
            logger.info("Eventos encontrados: {}", eventos.size());
        } else {
            logger.warn("No se encontraron procesos involucrados para el usuario ID: {}", usuario.getId());
        }

        model.addAttribute("eventos", eventos);
        model.addAttribute("usuarioActual", usuario);
        model.addAttribute("now", LocalDateTime.now());
        model.addAttribute("currentYear", LocalDateTime.now().getYear());
        model.addAttribute("currentMonth", LocalDateTime.now().getMonthValue() - 1);

         return "abogado".equalsIgnoreCase(usuario.getRol().getNombre()) ? 
               "abogado/calendario" : "cliente/calendario";
    }

    @PostMapping("/abogado/guardar-evento")
    public String guardarEvento(@ModelAttribute AudienciaEvento evento, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            // Validar procesoId
            if (evento.getProceso() == null || evento.getProceso().getId() == null) {
                return "redirect:/abogado/calendario?error=El%20campo%20procesoId%20es%20obligatorio";
            }
            Long procesoId = evento.getProceso().getId();
            Optional<ProcesoLegal> procesoOpt = procesoServicio.obtenerPorId(procesoId);
            if (!procesoOpt.isPresent()) {
                return "redirect:/abogado/calendario?error=El%20proceso%20especificado%20no%20existe";
            }

            // Validar que el proceso esté asociado al usuario
            List<ProcesoLegal> procesosInvolucrados = procesoServicio.listarPorUsuario(usuario.getId());
            boolean procesoValido = procesosInvolucrados.stream()
                    .anyMatch(p -> p.getId().equals(procesoId));
            if (!procesoValido) {
                return "redirect:/abogado/calendario?error=El%20proceso%20seleccionado%20no%20está%20asociado%20al%20usuario";
            }

            evento.setProceso(procesoOpt.get());
            if (evento.getId() != null) {
                // Buscar el evento original
                Optional<AudienciaEvento> eventoExistenteOpt = audienciaServicio.obtenerPorId(evento.getId());
                if (!eventoExistenteOpt.isPresent()) {
                    return "redirect:/abogado/calendario?error=El%20evento%20no%20existe";
                }
                AudienciaEvento eventoExistente = eventoExistenteOpt.get();

                // Actualizar solo los campos editables
                eventoExistente.setTipoEvento(evento.getTipoEvento());
                eventoExistente.setDescripcion(evento.getDescripcion());
                eventoExistente.setFechaEvento(evento.getFechaEvento());
                eventoExistente.setProceso(evento.getProceso());

                // Setear modificador
                eventoExistente.setModificadoPor(usuario);
                eventoExistente.setFechaModificacion(LocalDateTime.now());

                audienciaServicio.actualizarEvento(eventoExistente);
            } else {
                evento.setCreadoPor(usuario);
                evento.setFechaCreacion(LocalDateTime.now());
                audienciaServicio.crearEvento(evento);
            }

            return "redirect:/abogado/calendario?success=Evento%20guardado%20correctamente";
        } catch (Exception e) {
            return "redirect:/abogado/calendario?error=Error%20al%20guardar%20el%20evento:%20" + e.getMessage();
        }
    }

    @DeleteMapping("/eliminar-evento/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarEvento(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            boolean eliminado = audienciaServicio.eliminarEvento(id, usuario.getId());
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Evento no encontrado");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el evento: " + e.getMessage());
        }
    }
    
    @GetMapping("/eventos-por-fecha")
    @ResponseBody
    public ResponseEntity<List<AudienciaEvento>> obtenerEventosPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Obtener procesos del usuario
            List<ProcesoUsuario> procesosInvolucrados = procesoUsuarioServicio.listarPorUsuarioId(usuario.getId());
            
            if (procesosInvolucrados.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            // Obtener IDs de procesos
            List<Long> procesoIds = procesosInvolucrados.stream()
                .map(ProcesoUsuario::getProceso)
                .map(ProcesoLegal::getId)
                .collect(Collectors.toList());
            
            // Obtener eventos para la fecha específica
            List<AudienciaEvento> eventos = audienciaServicio.listarPorProcesoIdsYFecha(procesoIds, fecha);
            
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/abogado/eventos")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listarEventos(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            logger.warn("No hay usuario en la sesión para /abogado/eventos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<ProcesoUsuario> procesosInvolucrados = procesoUsuarioServicio.listarPorUsuarioId(usuario.getId());
            if (procesosInvolucrados.isEmpty()) {
                logger.info("No hay procesos involucrados para el usuario ID: {}", usuario.getId());
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<Long> procesoIds = procesosInvolucrados.stream()
                    .map(ProcesoUsuario::getProceso)
                    .map(ProcesoLegal::getId)
                    .collect(Collectors.toList());
            
            List<AudienciaEvento> eventos = audienciaServicio.listarPorProcesoIds(procesoIds);
            logger.info("Eventos enviados para el usuario ID {}: {}", usuario.getId(), eventos.size());
            
            // Devolver estructura simple en lugar de entidades
            List<Map<String, Object>> eventosSimples = eventos.stream().map(evento -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", evento.getId());
                map.put("tipoEvento", evento.getTipoEvento());
                map.put("descripcion", evento.getDescripcion());
                map.put("fechaEvento", evento.getFechaEvento());
                map.put("procesoId", evento.getProceso() != null ? evento.getProceso().getId() : null);
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(eventosSimples);
        } catch (Exception e) {
            logger.error("Error al listar eventos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
