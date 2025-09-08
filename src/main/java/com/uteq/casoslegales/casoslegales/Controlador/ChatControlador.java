package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.Chat;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.ChatServicio;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatControlador {

    @Autowired
    private ChatServicio chatServicio;

    @GetMapping("/admin/gestion_chats")
    public String listarChats(@RequestParam(defaultValue = "0") int pagina,
                              @RequestParam(defaultValue = "10") int tamano,
                              Model model) {
        Page<Chat> paginaChats = chatServicio.listarPaginados(pagina, tamano);

        model.addAttribute("chats", paginaChats.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaChats.getTotalPages());
        model.addAttribute("totalElementos", paginaChats.getTotalElements());

        return "admin/gestion_chats";
    }

    @PostMapping("/admin/guardar_chat")
    public String guardarChat(@ModelAttribute Chat dato, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        LocalDateTime fechaActual = LocalDateTime.now();
        dato.setCreadoPor(usuarioSesion);
        dato.setFechaCreacion(fechaActual);
        chatServicio.guardar(dato);
        return "redirect:/admin/gestion_chats";
    }

    @DeleteMapping("/eliminar_chat/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = chatServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
