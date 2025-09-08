package com.uteq.casoslegales.casoslegales.Controlador;

import com.uteq.casoslegales.casoslegales.Modelo.Documento;
import com.uteq.casoslegales.casoslegales.Modelo.ProcesoLegal;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.DocumentoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.ProcesoLegalServicio;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class DocumentoControlador {

    @Autowired
    private DocumentoServicio documentoServicio;

    @Autowired
    private ProcesoLegalServicio procesoServicio;

    @GetMapping("/admin/gestion_documentos_agregar")
    public String mostrarFormularioDocumento(Model model) {
        model.addAttribute("documento", new Documento());
        
        // Obtener procesos en formato mínimo [id, numeroProceso, nombreCliente]
        List<Object[]> procesosMinimos = procesoServicio.listarProcesoLegalMinimo();
        model.addAttribute("procesosMinimos", procesosMinimos);
        
        return "admin/gestion_documentos_agregar";
    }

    @PostMapping(value = "/admin/guardar_documento", consumes = "multipart/form-data")
    public String guardarDocumento(
            @ModelAttribute Documento documento,
            @RequestParam("archivo") MultipartFile archivo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Validar que no sea una solicitud de favicon u otro recurso no deseado
            if (archivo.getOriginalFilename() != null && 
                archivo.getOriginalFilename().toLowerCase().endsWith(".ico")) {
                System.out.println("Archivo .ico detectado, ignorando...");
                return "redirect:/admin/gestion_documentos_agregar";
            }

            Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
            LocalDateTime fechaActual = LocalDateTime.now();

            // Validar archivo
            if (archivo.isEmpty()) {
                System.out.println("Archivo vacío detectado");
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar un archivo");
                return "redirect:/admin/gestion_documentos_agregar";
            }

            // Validar tamaño del archivo (10MB máximo)
            if (archivo.getSize() > 10 * 1024 * 1024) {
                System.out.println("Archivo demasiado grande: " + archivo.getSize());
                redirectAttributes.addFlashAttribute("error", "El archivo excede el tamaño máximo permitido (10MB)");
                return "redirect:/admin/gestion_documentos_agregar";
            }

            // Validar que se haya seleccionado un proceso
            if (documento.getProceso() == null || documento.getProceso().getId() == null) {
                System.out.println("Proceso no seleccionado");
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar un proceso legal");
                return "redirect:/admin/gestion_documentos_agregar";
            }

            System.out.println("Proceso ID seleccionado: " + documento.getProceso().getId());

            // Obtener el proceso completo para establecer la relación
            Optional<ProcesoLegal> procesoOpt = procesoServicio.obtenerPorId(documento.getProceso().getId());
            if (procesoOpt.isEmpty()) {
                System.out.println("Proceso no encontrado con ID: " + documento.getProceso().getId());
                redirectAttributes.addFlashAttribute("error", "Proceso no encontrado con ID: " + documento.getProceso().getId());
                return "redirect:/admin/gestion_documentos_agregar";
            }

            ProcesoLegal proceso = procesoOpt.get();
            System.out.println("Proceso encontrado: " + proceso.getNumeroProceso());

            // Guardar archivo en el sistema de archivos
            String nombreArchivo = guardarArchivo(archivo);
            String rutaArchivo = "/documentos/" + nombreArchivo;
            System.out.println("Archivo guardado en: " + rutaArchivo);

            // Configurar entidad Documento
            documento.setRutaArchivo(rutaArchivo);
            documento.setCreadoPor(usuarioSesion);
            documento.setFechaCreacion(fechaActual);
            documento.setProceso(proceso);

            System.out.println("Documento a guardar: " + documento.toString());

            // Guardar documento
            Documento documentoGuardado = documentoServicio.guardar(documento);
            System.out.println("Documento guardado con ID: " + documentoGuardado.getId());

            redirectAttributes.addFlashAttribute("exito", true);
            return "redirect:/admin/gestion_documentos_agregar";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar el documento: " + e.getMessage());
            return "redirect:/admin/gestion_documentos_agregar";
        }
    }

    // Método para guardar archivo
    public String guardarArchivo(MultipartFile archivo) {
        try {
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

            return nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    @GetMapping("/admin/gestion_documentos")
    public String listarDocumentos(@RequestParam(defaultValue = "0") int pagina,
                                   @RequestParam(defaultValue = "10") int tamano,
                                   Model model) {
        Page<Documento> paginaDocumentos = documentoServicio.listarPaginados(pagina, tamano);

        model.addAttribute("documentos", paginaDocumentos.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaDocumentos.getTotalPages());
        model.addAttribute("totalElementos", paginaDocumentos.getTotalElements());

        return "admin/gestion_documentos";
    }

    @DeleteMapping("/eliminar_documento/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            boolean eliminado = documentoServicio.eliminarPorId(id);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}