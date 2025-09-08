package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.ChatUsuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ChatUsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatUsuarioServicio {

    @Autowired
    private ChatUsuarioRepositorio chatUsuarioRepo;

    public List<ChatUsuario> listarTodos() {
        return chatUsuarioRepo.findAll();
    }

    public Optional<ChatUsuario> obtenerPorId(Long id) {
        return chatUsuarioRepo.findById(id);
    }

    public ChatUsuario guardar(ChatUsuario chatUsuario) {
        return chatUsuarioRepo.save(chatUsuario);
    }

    public void eliminar(Long id) {
        chatUsuarioRepo.deleteById(id);
    }
}
