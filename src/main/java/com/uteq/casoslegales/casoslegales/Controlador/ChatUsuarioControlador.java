package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.ChatUsuario;
import com.uteq.casoslegales.casoslegales.Servicio.ChatUsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat-usuarios")
@CrossOrigin("*")
public class ChatUsuarioControlador {

    @Autowired
    private ChatUsuarioServicio chatUsuarioServicio;

    @GetMapping
    public List<ChatUsuario> listarTodos() {
        return chatUsuarioServicio.listarTodos();
    }

    @GetMapping("/{id}")
    public Optional<ChatUsuario> obtenerPorId(@PathVariable Long id) {
        return chatUsuarioServicio.obtenerPorId(id);
    }

    @PostMapping
    public ChatUsuario guardar(@RequestBody ChatUsuario chatUsuario) {
        return chatUsuarioServicio.guardar(chatUsuario);
    }

    @PutMapping("/{id}")
    public ChatUsuario actualizar(@PathVariable Long id, @RequestBody ChatUsuario chatUsuario) {
        chatUsuario.setId(id);
        return chatUsuarioServicio.guardar(chatUsuario);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        chatUsuarioServicio.eliminar(id);
    }
}
