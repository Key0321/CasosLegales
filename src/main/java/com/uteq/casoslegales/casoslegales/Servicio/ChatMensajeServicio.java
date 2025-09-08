package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.ChatMensaje;
import com.uteq.casoslegales.casoslegales.Repositorio.ChatMensajeRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatMensajeServicio {

    @Autowired
    private ChatMensajeRepositorio chatMensajeRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatMensaje> listarTodos() {
        return chatMensajeRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<ChatMensaje> obtenerPorId(Long id) {
        return chatMensajeRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChatMensaje guardar(ChatMensaje mensaje) {
        return chatMensajeRepo.save(mensaje);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        chatMensajeRepo.deleteById(id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatMensaje> listarPorChatId(Long chatId) {
        return chatMensajeRepo.findByChatIdAndEliminadoPorIsNull(chatId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatMensaje> obtenerMensajesPorChat(Long chatId) {
        return chatMensajeRepo.findByChatIdWithEmisorAndEliminadoPorIsNull(chatId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatMensaje> obtenerMensajesPosteriores(Long chatId, Long ultimoId) {
        return chatMensajeRepo.findByChatIdAndIdGreaterThanAndEliminadoPorIsNull(chatId, ultimoId);
    }
}
