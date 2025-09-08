package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Chat;
import com.uteq.casoslegales.casoslegales.Repositorio.ChatRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatServicio {

    @Autowired
    private ChatRepositorio chatRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Chat> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return chatRepo.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (chatRepo.existsById(id)) {
            chatRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Chat> listarTodos() {
        return chatRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Chat> obtenerPorId(Long id) {
        return chatRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        chatRepo.deleteById(id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Chat> obtenerPorProcesoId(Long procesoId) {
        return chatRepo.findByProcesoIdAndEliminadoPorIsNull(procesoId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Chat guardar(Chat chat) {
        if (chat.getId() == null) {
            chat.setFechaCreacion(LocalDateTime.now());
        }
        return chatRepo.save(chat);
    }
}
