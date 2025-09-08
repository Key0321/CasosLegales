package com.uteq.casoslegales.casoslegales.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMensajeDto {
    private Long id;
    private String contenido;
    private Long procesoId;
    private String tipoContenido;
    private Long emisorId;
    private String emisorNombre;
    private String fechaEnvio;
    private Long chatId;
    // Getters y setters
}