package com.uteq.casoslegales.casoslegales.Servicio;

import com.uteq.casoslegales.casoslegales.Modelo.Documento;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.DocumentoRepositorio;

import jakarta.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Propagation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Path;

@Service
@Transactional
public class DocumentoServicio {

    @Autowired
    private DocumentoRepositorio documentoRepo;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Documento> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return documentoRepo.findAll(pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (documentoRepo.existsById(id)) {
            documentoRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminarPorId(Long id, Usuario usuario) {
        Optional<Documento> docOpt = documentoRepo.findByIdAndFechaEliminacionIsNull(id);
        if (docOpt.isPresent()) {
            Documento doc = docOpt.get();
            doc.setEliminadoPor(usuario);
            doc.setFechaEliminacion(LocalDateTime.now());
            documentoRepo.save(doc);
        } else {
            throw new RuntimeException("Documento no encontrado o ya eliminado");
        }
    }
    
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Documento> listarTodos() {
        return documentoRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Documento> listarPorProcesoId(Long procesoId) {
        return documentoRepo.findByProcesoIdAndEliminadoPorIsNull(procesoId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Documento> obtenerPorId(Long id) {
        return documentoRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Documento guardar(Documento documento) {
        return documentoRepo.save(documento);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        documentoRepo.deleteById(id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, List<Documento>> obtenerAgrupadosPorFecha(Long procesoId) {
        List<Documento> documentos = documentoRepo.findByProcesoIdAndEliminadoPorIsNull(procesoId);
        Map<String, List<Documento>> agrupados = new HashMap<>();

        LocalDate hoy = LocalDate.now();
        documentos.forEach(doc -> {
            LocalDate fechaDoc = LocalDate.of(doc.getAnio(), doc.getMes(), 1);
            String clave = ChronoUnit.DAYS.between(fechaDoc, hoy) <= 0 ? "Hoy" : String.format("%d/%02d", doc.getAnio(), doc.getMes());
            agrupados.computeIfAbsent(clave, k -> new ArrayList<>()).add(doc);
        });

        return agrupados;
    }

    @Transactional(rollbackFor = Exception.class)
    public String guardarArchivo(MultipartFile archivo) throws IOException {
        // Validar archivo
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }
        if (archivo.getOriginalFilename() == null) {
            throw new IllegalArgumentException("El archivo no tiene un nombre válido");
        }
        if (archivo.getOriginalFilename().toLowerCase().endsWith(".ico")) {
            throw new IllegalArgumentException("Archivos .ico no están permitidos");
        }
        if (archivo.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (10MB)");
        }

        // Crear directorio si no existe
        Path uploadsDir = Paths.get("uploads/documentos");
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        // Generar nombre único para el archivo
        String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
        String extension = "";
        int i = nombreOriginal.lastIndexOf('.');
        if (i > 0) {
            extension = nombreOriginal.substring(i);
            nombreOriginal = nombreOriginal.substring(0, i);
        }
        String nombreArchivo = nombreOriginal + "_" + System.currentTimeMillis() + extension;

        // Guardar archivo
        Path filePath = uploadsDir.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Devolver ruta completa
        return "/documentos/" + nombreArchivo;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Documento> listarPaginadosConFiltrosPorUsuario(String busqueda, Long proceso, Integer anio, String tipo, List<Long> procesoIds, int pagina, int tamano) {
    Specification<Documento> spec = (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();
        // Filtrar por procesos del usuario
        predicates.add(root.get("proceso").get("id").in(procesoIds));
        // Otros filtros
        if (busqueda != null && !busqueda.isEmpty()) {
            String busquedaLower = "%" + busqueda.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("nombre")), busquedaLower),
                    cb.like(cb.lower(root.get("palabrasClave")), busquedaLower)
            ));
        }
        if (proceso != null) {
            predicates.add(cb.equal(root.get("proceso").get("id"), proceso));
        }
        if (anio != null) {
            predicates.add(cb.equal(root.get("anio"), anio));
        }
        if (tipo != null && !tipo.isEmpty()) {
            predicates.add(cb.equal(root.get("tipoDocumento"), tipo));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    };
    return documentoRepo.findAll(spec, PageRequest.of(pagina, tamano));
}

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ProcesoLegal> obtenerProcesosConDocumentosPorUsuario(List<Long> procesoIds) {
        return documentoRepo.findDistinctProcesosByProcesoIds(procesoIds);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Integer> obtenerAniosDocumentosPorUsuario(List<Long> procesoIds) {
        return documentoRepo.findDistinctAniosByProcesoIds(procesoIds);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<String> obtenerTiposDocumentosPorUsuario(List<Long> procesoIds) {
        return documentoRepo.findDistinctTiposByProcesoIds(procesoIds);
    }

}
