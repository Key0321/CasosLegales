package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Notificacion;
import com.uteq.casoslegales.casoslegales.Repositorio.NotificacionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificacionServicio {

    @Autowired
    private NotificacionRepositorio notificacionRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Notificacion> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return notificacionRepo.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (notificacionRepo.existsById(id)) {
            notificacionRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Notificacion> listarTodos() {
        return notificacionRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Notificacion> obtenerPorId(Long id) {
        return notificacionRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Notificacion guardar(Notificacion notificacion) {
        return notificacionRepo.save(notificacion);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        notificacionRepo.deleteById(id);
    }
}
