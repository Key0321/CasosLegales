package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.ChatMensaje;
import com.uteq.casoslegales.casoslegales.Repositorio.ChatMensajeRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChatMensajeServicio {

    @Autowired
    private ChatMensajeRepositorio chatMensajeRepo;

    public List<ChatMensaje> listarTodos() {
        return chatMensajeRepo.findAll();
    }

    public Optional<ChatMensaje> obtenerPorId(Long id) {
        return chatMensajeRepo.findById(id);
    }

    public ChatMensaje guardar(ChatMensaje mensaje) {
        return chatMensajeRepo.save(mensaje);
    }

    public void eliminar(Long id) {
        chatMensajeRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ChatMensaje> listarPorChatId(Long chatId) {
        return chatMensajeRepo.findByChatIdAndEliminadoPorIsNull(chatId);
    }

    public List<ChatMensaje> obtenerMensajesPorChat(Long chatId) {
        return chatMensajeRepo.findByChatIdWithEmisorAndEliminadoPorIsNull(chatId);
    }

    public List<ChatMensaje> obtenerMensajesPosteriores(Long chatId, Long ultimoId) {
        return chatMensajeRepo.findByChatIdAndIdGreaterThanAndEliminadoPorIsNull(chatId, ultimoId);
    }
}
