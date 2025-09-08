package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.DTOs.ChatMensajeDto;
import com.uteq.casoslegales.casoslegales.Modelo.Chat;
import com.uteq.casoslegales.casoslegales.Modelo.ChatMensaje;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.ChatMensajeServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ChatServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoLegalServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoUsuarioServicio;
import com.uteq.casoslegales.casoslegales.Servicio.UsuarioServicio;

import jakarta.servlet.http.HttpSession;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ChatMensajeControlador {

    private static final Logger logger = LoggerFactory.getLogger(ChatMensajeControlador.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatServicio chatServicio;

    @Autowired
    private ChatMensajeServicio chatMensajeServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ProcesoUsuarioServicio procesoUsuarioServicio;

    @Autowired
    private ProcesoLegalServicio procesoServicio;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMensajeDto chatMensajeDto) {
        try {
            logger.info("Recibiendo mensaje para proceso: {}", chatMensajeDto.getProcesoId());
            logger.info("Mensaje completo: {}", chatMensajeDto);

            if (chatMensajeDto.getEmisorId() == null) {
                throw new IllegalArgumentException("EmisorId es requerido");
            }
            if (chatMensajeDto.getProcesoId() == null) {
                throw new IllegalArgumentException("ProcesoId es requerido");
            }

            Optional<Usuario> usuarioOpt = usuarioServicio.buscarPorId(chatMensajeDto.getEmisorId());
            Usuario usuario = usuarioOpt.orElseThrow(() -> 
                new IllegalArgumentException("Usuario no encontrado con ID: " + chatMensajeDto.getEmisorId()));

            Optional<Chat> chatOpt = chatServicio.obtenerPorProcesoId(chatMensajeDto.getProcesoId());
            Chat chat = chatOpt.orElseGet(() -> {
                Optional<ProcesoLegal> procesoOpt = procesoServicio.obtenerPorId(chatMensajeDto.getProcesoId());
                if (procesoOpt.isPresent()) {
                    Chat nuevoChat = new Chat();
                    nuevoChat.setTitulo(procesoOpt.get().getNumeroProceso());
                    nuevoChat.setProceso(procesoOpt.get());
                    nuevoChat.setCreadoPor(usuario);
                    nuevoChat.setFechaCreacion(LocalDateTime.now());
                    return chatServicio.guardar(nuevoChat);
                }
                throw new IllegalArgumentException("Proceso no encontrado con ID: " + chatMensajeDto.getProcesoId());
            });

            ChatMensaje mensaje = new ChatMensaje();
            mensaje.setChat(chat);
            mensaje.setEmisor(usuario);
            mensaje.setContenido(chatMensajeDto.getContenido());
            mensaje.setTipoContenido("TEXTO");
            mensaje.setFechaEnvio(LocalDateTime.now());
            mensaje.setCreadoPor(usuario);
            mensaje.setFechaCreacion(LocalDateTime.now());

            ChatMensaje mensajeGuardado = chatMensajeServicio.guardar(mensaje);

            ChatMensajeDto respuesta = convertirADto(mensajeGuardado);
            respuesta.setProcesoId(chatMensajeDto.getProcesoId());

            String topic = "/topic/chat." + chatMensajeDto.getProcesoId();
            logger.info("Enviando mensaje a topic {}: {}", topic, respuesta);
            messagingTemplate.convertAndSend(topic, respuesta);

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al enviar mensaje: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al enviar mensaje: {}", e.getMessage(), e);
            throw new RuntimeException("Error al enviar mensaje: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMensajeDto chatMensajeDto) {
        try {
            logger.info("Usuario conectado al chat para proceso: {}", chatMensajeDto.getProcesoId());

            if (chatMensajeDto.getProcesoId() == null) {
                throw new IllegalArgumentException("ProcesoId es requerido");
            }

            Optional<Usuario> usuarioOpt = usuarioServicio.buscarPorId(chatMensajeDto.getEmisorId());
            if (usuarioOpt.isEmpty()) {
                throw new IllegalArgumentException("Usuario no encontrado con ID: " + chatMensajeDto.getEmisorId());
            }

            String topic = "/topic/chat." + chatMensajeDto.getProcesoId();
            logger.info("Enviando mensaje de conexión a topic {}: {}", topic, chatMensajeDto);
            messagingTemplate.convertAndSend(topic, chatMensajeDto);

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al agregar usuario: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al agregar usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error al agregar usuario: " + e.getMessage());
        }
    }

    @MessageMapping("/test")
    public void testMessage(@Payload Map<String, Object> payload) {
        logger.info("Mensaje de prueba recibido: {}", payload);
        messagingTemplate.convertAndSend("/topic/test", payload);
    }

    @GetMapping("/abogado/mensajes-proceso/{procesoId}")
    @ResponseBody
    public ResponseEntity<?> listarMensajesPorProceso(@PathVariable Long procesoId, 
                                                     @RequestParam Long ultimoId,
                                                     HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            logger.warn("No hay usuario en la sesión para /abogado/mensajes-proceso/{}", procesoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        if (!"admin".equalsIgnoreCase(usuario.getRol().getNombre()) &&
            !procesoUsuarioServicio.estaUsuarioInvolucrado(procesoId, usuario.getId())) {
            logger.warn("Usuario {} no tiene acceso al proceso con ID: {}", usuario.getId(), procesoId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }

        try {
            Optional<Chat> chatOpt = chatServicio.obtenerPorProcesoId(procesoId);
            if (chatOpt.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<ChatMensaje> mensajes = chatMensajeServicio.obtenerMensajesPosteriores(chatOpt.get().getId(), ultimoId);
            List<ChatMensajeDto> dtos = mensajes.stream()
                .map(this::convertirADto)
                .peek(dto -> dto.setProcesoId(procesoId))
                .collect(Collectors.toList());
            logger.info("Mensajes enviados para proceso {}: {}", procesoId, dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error al listar mensajes para el proceso {}: {}", procesoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener mensajes");
        }
    }

    private ChatMensajeDto convertirADto(ChatMensaje mensaje) {
        ChatMensajeDto dto = new ChatMensajeDto();
        dto.setId(mensaje.getId());
        dto.setContenido(mensaje.getContenido());
        dto.setEmisorId(mensaje.getEmisor().getId());
        dto.setEmisorNombre(mensaje.getEmisor().getNombre() + " " + mensaje.getEmisor().getApellido());
        dto.setFechaEnvio(mensaje.getFechaEnvio().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setTipoContenido(mensaje.getTipoContenido());
        dto.setChatId(mensaje.getChat().getId());
        return dto;
    }
}