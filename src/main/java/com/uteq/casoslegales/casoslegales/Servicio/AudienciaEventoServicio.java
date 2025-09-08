package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.AudienciaEvento;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.AudienciaEventoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AudienciaEventoServicio {

    @Autowired
    private AudienciaEventoRepositorio audienciaRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<AudienciaEvento> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return audienciaRepo.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (audienciaRepo.existsById(id)) {
            audienciaRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<AudienciaEvento> listarTodos() {
        return audienciaRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<AudienciaEvento> obtenerPorId(Long id) {
        return audienciaRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public AudienciaEvento guardar(AudienciaEvento evento) {
        return audienciaRepo.save(evento);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        audienciaRepo.deleteById(id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<AudienciaEvento> listarPorProcesoId(Long procesoId) {
        return audienciaRepo.findByProcesoIdAndEliminadoPorIsNull(procesoId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<AudienciaEvento> listarPorProcesoIds(List<Long> procesoIds) {
        return audienciaRepo.findByProcesoIdInAndEliminadoPorIsNull(procesoIds);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<AudienciaEvento> listarPorProcesoIdsYFecha(List<Long> procesoIds, LocalDate fecha) {
        LocalDateTime startOfDay = fecha.atStartOfDay();
        LocalDateTime endOfDay = fecha.atTime(23, 59, 59);
        return audienciaRepo.findByProcesoIdInAndFechaEventoBetween(procesoIds, startOfDay, endOfDay);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public AudienciaEvento crearEvento(AudienciaEvento evento) {
        return audienciaRepo.save(evento);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public AudienciaEvento actualizarEvento(AudienciaEvento evento) {
        Optional<AudienciaEvento> existingEvent = audienciaRepo.findById(evento.getId());
        if (existingEvent.isPresent()) {
            AudienciaEvento updatedEvent = existingEvent.get();
            updatedEvent.setTipoEvento(evento.getTipoEvento());
            updatedEvent.setDescripcion(evento.getDescripcion());
            updatedEvent.setFechaEvento(evento.getFechaEvento());
            updatedEvent.setFechaModificacion(LocalDateTime.now());
            return audienciaRepo.save(updatedEvent);
        }
        throw new RuntimeException("Evento no encontrado con ID: " + evento.getId());
    }
    
    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarEvento(Long id, Long usuarioId) {
        Optional<AudienciaEvento> evento = audienciaRepo.findById(id);
        if (evento.isPresent() && evento.get().getCreadoPor().getId().equals(usuarioId)) {
            AudienciaEvento eventoToDelete = evento.get();
            eventoToDelete.setEliminadoPor(eventoToDelete.getCreadoPor());
            eventoToDelete.setFechaEliminacion(LocalDateTime.now());
            audienciaRepo.save(eventoToDelete);
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminarPorId(Long id, Usuario usuario) {
        Optional<AudienciaEvento> eventoOpt = audienciaRepo.findByIdAndFechaEliminacionIsNull(id);
        if (eventoOpt.isPresent()) {
            AudienciaEvento evento = eventoOpt.get();
            evento.setEliminadoPor(usuario);
            evento.setFechaEliminacion(LocalDateTime.now());
            audienciaRepo.save(evento);
        } else {
            throw new RuntimeException("Evento no encontrado o ya eliminado");
        }
    }
}
