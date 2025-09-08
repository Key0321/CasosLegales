package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Acceso;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.AccesoRepositorio;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccesoServicio {

    @Autowired
    private AccesoRepositorio accesoRepo;

    public List<Acceso> listarTodos() {
        return accesoRepo.findAll();
    }

    @Transactional
    public void registrarSalida(Long accesoId) {
        Acceso acceso = accesoRepo.findById(accesoId).orElse(null);
        if (acceso != null && acceso.getFechaSalida() == null) {
            acceso.setFechaSalida(LocalDateTime.now());
            accesoRepo.save(acceso);
        }
    }

    public Acceso obtenerUltimoAcceso(Usuario usuario) {
        List<Acceso> accesos = accesoRepo.findUltimoAccesoByUsuario(usuario, PageRequest.of(0,1));
        return accesos.isEmpty() ? null : accesos.get(0);
    }

    public Acceso obtenerUltimoAccesoPorUsuarioId(Long usuarioId) {
        return accesoRepo.findTopByUsuarioIdOrderByFechaIngresoDesc(usuarioId);
    }

    public Optional<Acceso> obtenerPorId(Long id) {
        return accesoRepo.findById(id);
    }

    public Acceso guardar(Acceso acceso) {
        return accesoRepo.save(acceso);
    }

    public void eliminar(Long id) {
        accesoRepo.deleteById(id);
    }
}
