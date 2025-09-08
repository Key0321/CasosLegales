package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Notificacion;
import com.uteq.casoslegales.casoslegales.Repositorio.NotificacionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacionServicio {

    @Autowired
    private NotificacionRepositorio notificacionRepo;

    public Page<Notificacion> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return notificacionRepo.findAll(pageable);
    }

    public boolean eliminarPorId(Long id) {
        if (notificacionRepo.existsById(id)) {
            notificacionRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Notificacion> listarTodos() {
        return notificacionRepo.findAll();
    }

    public Optional<Notificacion> obtenerPorId(Long id) {
        return notificacionRepo.findById(id);
    }

    public Notificacion guardar(Notificacion notificacion) {
        return notificacionRepo.save(notificacion);
    }

    public void eliminar(Long id) {
        notificacionRepo.deleteById(id);
    }
}
