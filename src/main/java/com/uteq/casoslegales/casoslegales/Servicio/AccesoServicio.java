package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Acceso;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.AccesoRepositorio;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AccesoServicio {

    @Autowired
    private AccesoRepositorio accesoRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Acceso> listarTodos() {
        return accesoRepo.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void registrarSalida(Long accesoId) {
        Acceso acceso = accesoRepo.findById(accesoId).orElse(null);
        if (acceso != null && acceso.getFechaSalida() == null) {
            acceso.setFechaSalida(LocalDateTime.now());
            accesoRepo.save(acceso);
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Acceso obtenerUltimoAcceso(Usuario usuario) {
        List<Acceso> accesos = accesoRepo.findUltimoAccesoByUsuario(usuario, PageRequest.of(0,1));
        return accesos.isEmpty() ? null : accesos.get(0);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Acceso obtenerUltimoAccesoPorUsuarioId(Long usuarioId) {
        return accesoRepo.findTopByUsuarioIdOrderByFechaIngresoDesc(usuarioId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Acceso> obtenerPorId(Long id) {
        return accesoRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Acceso guardar(Acceso acceso) {
        return accesoRepo.save(acceso);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        accesoRepo.deleteById(id);
    }
}
