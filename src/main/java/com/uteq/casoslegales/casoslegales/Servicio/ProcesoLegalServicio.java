package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ProcesoLegalRepositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProcesoLegalServicio {

    @Autowired
    private ProcesoLegalRepositorio procesoRepo;


    public Page<ProcesoLegal> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return procesoRepo.findAll(pageable);
    }

    public List<Object[]> listarProcesoLegalMinimo() {
        return procesoRepo.listarProcesosSoloCampos();
    }

    @Transactional
    public boolean eliminarPorId(Long id, Usuario usuario) {
        Optional<ProcesoLegal> procesoOpt = procesoRepo.findByIdAndEliminadoPorIsNull(id);
        if (procesoOpt.isEmpty()) {
            return false;
        }
        ProcesoLegal proceso = procesoOpt.get();
        proceso.setEliminadoPor(usuario);
        proceso.setFechaEliminacion(LocalDateTime.now());
        procesoRepo.save(proceso);
        return true;
    }

    public Optional<ProcesoLegal> obtenerPorId(Long id) {
        return procesoRepo.findById(id);
    }

    public ProcesoLegal guardar(ProcesoLegal proceso) {
        return procesoRepo.save(proceso);
    }

    public void eliminar(Long id) {
        procesoRepo.deleteById(id);
    }

    public List<ProcesoLegal> listarPorUsuario(Long usuarioId) {
        List<ProcesoLegal> procesosPorUsuario = procesoRepo.findProcesosLegalesByUsuarioId(usuarioId);
        List<ProcesoLegal> procesosPorCliente = procesoRepo.findProcesosLegalesByClienteUsuarioId(usuarioId);
        Set<ProcesoLegal> procesosUnicos = new HashSet<>();
        procesosUnicos.addAll(procesosPorUsuario);
        procesosUnicos.addAll(procesosPorCliente);
        
        // Convertir el Set a List y ordenar por fechaCreacion de más reciente a más antigua
        List<ProcesoLegal> procesosOrdenados = new ArrayList<>(procesosUnicos);
        procesosOrdenados.sort((p1, p2) -> p2.getFechaCreacion().compareTo(p1.getFechaCreacion()));
        
        return procesosOrdenados;
    }
}
