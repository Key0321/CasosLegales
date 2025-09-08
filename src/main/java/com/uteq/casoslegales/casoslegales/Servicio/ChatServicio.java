package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Chat;
import com.uteq.casoslegales.casoslegales.Repositorio.ChatRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatServicio {

    @Autowired
    private ChatRepositorio chatRepo;

    public Page<Chat> listarPaginados(int pagina, int tamaño) {
    Pageable pageable = PageRequest.of(pagina, tamaño);
        return chatRepo.findAll(pageable);
    }

    public boolean eliminarPorId(Long id) {
        if (chatRepo.existsById(id)) {
            chatRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Chat> listarTodos() {
        return chatRepo.findAll();
    }

    public Optional<Chat> obtenerPorId(Long id) {
        return chatRepo.findById(id);
    }


    public void eliminar(Long id) {
        chatRepo.deleteById(id);
    }

     @Transactional(readOnly = true)
    public Optional<Chat> obtenerPorProcesoId(Long procesoId) {
        return chatRepo.findByProcesoIdAndEliminadoPorIsNull(procesoId);
    }

    @Transactional
    public Chat guardar(Chat chat) {
        if (chat.getId() == null) {
            chat.setFechaCreacion(LocalDateTime.now());
        }
        return chatRepo.save(chat);
    }
}
