package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.ProcesoUsuario;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ProcesoUsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProcesoUsuarioServicio {

    @Autowired
    private ProcesoUsuarioRepositorio procesoUsuarioRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ProcesoUsuario> listarTodos() {
        return procesoUsuarioRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<ProcesoUsuario> obtenerPorId(Long id) {
        return procesoUsuarioRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProcesoUsuario guardar(ProcesoUsuario procesoUsuario) {
        return procesoUsuarioRepo.save(procesoUsuario);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        procesoUsuarioRepo.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminarPorProcesoYUsuario(Long procesoId, Long usuarioId, Usuario usuarioSesion) {
        Optional<ProcesoUsuario> puOpt = procesoUsuarioRepo.findByProcesoIdAndUsuarioIdAndFechaEliminacionIsNull(procesoId, usuarioId);
        if (puOpt.isPresent()) {
            ProcesoUsuario pu = puOpt.get();
            pu.setEliminadoPor(usuarioSesion);
            pu.setFechaEliminacion(LocalDateTime.now());
            procesoUsuarioRepo.save(pu);
        } else {
            throw new RuntimeException("Relación usuario-proceso no encontrada o ya eliminada");
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean estaUsuarioInvolucrado(Long procesoId, Long usuarioId) {
        return procesoUsuarioRepo.existsByProcesoIdAndUsuarioIdAndEliminadoPorIsNull(procesoId, usuarioId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ProcesoUsuario> listarPorProcesoId(Long procesoId) {
        return procesoUsuarioRepo.findByProcesoIdAndEliminadoPorIsNull(procesoId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ProcesoUsuario> listarPorUsuarioId(Long usuarioId) {
        return procesoUsuarioRepo.findByUsuarioIdAndEliminadoPorIsNull(usuarioId);
    }

}
