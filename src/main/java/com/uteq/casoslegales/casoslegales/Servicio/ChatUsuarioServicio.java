package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.ChatUsuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ChatUsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatUsuarioServicio {

    @Autowired
    private ChatUsuarioRepositorio chatUsuarioRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatUsuario> listarTodos() {
        return chatUsuarioRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<ChatUsuario> obtenerPorId(Long id) {
        return chatUsuarioRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChatUsuario guardar(ChatUsuario chatUsuario) {
        return chatUsuarioRepo.save(chatUsuario);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        chatUsuarioRepo.deleteById(id);
    }
}
