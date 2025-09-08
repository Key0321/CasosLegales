package com.uteq.casoslegales.casoslegales.Repositorio;

import com.uteq.casoslegales.casoslegales.Modelo.ChatMensaje;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMensajeRepositorio extends JpaRepository<ChatMensaje, Long> {
    List<ChatMensaje> findByChatIdAndEliminadoPorIsNull(Long chatId);

    @Query("SELECT m FROM ChatMensaje m JOIN FETCH m.emisor WHERE m.chat.id = :chatId ORDER BY m.fechaEnvio ASC")
    List<ChatMensaje> findByChatIdWithEmisorAndEliminadoPorIsNull(@Param("chatId") Long chatId);

    @Query("SELECT m FROM ChatMensaje m JOIN FETCH m.emisor WHERE m.chat.id = :chatId AND m.id > :ultimoId ORDER BY m.fechaEnvio ASC")
    List<ChatMensaje> findByChatIdAndIdGreaterThanAndEliminadoPorIsNull(@Param("chatId") Long chatId, @Param("ultimoId") Long ultimoId);
}
