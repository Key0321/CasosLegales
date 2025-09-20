package com.uteq.casoslegales.casoslegales.Servicio;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.jdbc.core.JdbcTemplate;

import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Repositorio.ProcesoUsuarioRepositorio;
import com.uteq.casoslegales.casoslegales.Repositorio.UsuarioRepo;

@Service
@Transactional
public class UsuarioServicio {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProcesoUsuarioRepositorio procesoUsuarioRepositorio;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Usuario> autenticar(String correo, String contrasenia) {
        return usuarioRepo.findByCorreo(correo)
                .filter(u -> 
                    passwordEncoder.matches(contrasenia, u.getContrasenia()) 
                    || u.getContrasenia().equals(contrasenia)
                );
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Usuario> listarPaginados(int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        return usuarioRepo.findAll(pageable);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepo.findByCorreo(email);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Usuario> listarNoInvolucradosEnProceso(Long procesoId) {
        // Obtener IDs de usuarios involucrados en el proceso
        List<Long> usuariosInvolucradosIds = procesoUsuarioRepositorio
                .findByProcesoIdAndEliminadoPorIsNull(procesoId)
                .stream()
                .map(pu -> pu.getUsuario().getId())
                .collect(Collectors.toList());

        // Listar todos los usuarios que no estén en la lista de involucrados
        return usuarioRepo.findAll()
                .stream()
                .filter(usuario -> !usuariosInvolucradosIds.contains(usuario.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean eliminarPorId(Long id) {
        if (usuarioRepo.existsById(id)) {
            usuarioRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Object[]> listarUsuarioMinimo() {
        return usuarioRepo.listarUsuarioCampos();
    }

    @Transactional(rollbackFor = Exception.class)
    public Usuario guardar(Usuario usuario) {
        return usuarioRepo.save(usuario);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Usuario> listarTodos() {
        return usuarioRepo.findAll();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepo.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void eliminar(Long id) {
        usuarioRepo.deleteById(id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Usuario> listarPaginadosConFiltro(String busqueda, Long creadorId, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano);
        
        if (busqueda != null && !busqueda.isEmpty()) {
            return usuarioRepo.findByCreadorIdAndBusqueda(creadorId, busqueda, pageable);
        } else {
            return usuarioRepo.findByCreadoPor_Id(creadorId, pageable);
        }
    }

    public String generarContraseniaAleatoria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder contrasenia = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(caracteres.length());
            contrasenia.append(caracteres.charAt(index));
        }
        return contrasenia.toString();
    }

    public boolean existeContrasenia(String contrasenia) {
        String contraseniaEncriptada = passwordEncoder.encode(contrasenia);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM usuarios WHERE contrasenia = ?", Integer.class, contraseniaEncriptada);
        return count != null && count > 0;
    }

    public void enviarCorreoRegistro(String correoDestino, String contrasenia) throws MessagingException {
        String remitente = "kbedonv@uteq.edu.ec"; 
        String contraseniaRemitente = "gxvm oryp sexd nfqc"; 

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, contraseniaRemitente);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
            message.setSubject("Registro Exitoso - Nueva Contraseña");
            message.setText("Hola,\n\nTu cuenta ha sido creada exitosamente. Usa la siguiente contraseña para acceder al sistema:\n\nContraseña: " + contrasenia + "\n\nPor favor, cámbiala al iniciar sesión por seguridad.\n\nSaludos,\nEl equipo de soporte");
            Transport.send(message);
        } catch (AddressException e) {
            throw new MessagingException("Error en el formato de la dirección de correo: " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new MessagingException("Error al procesar o enviar el correo: " + e.getMessage(), e);
        }
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepo.findByCorreo(correo);
    }

    public void actualizarContrasenia(String correo, String nuevaContrasenia) {
        Optional<Usuario> usuarioOpt = usuarioRepo.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Hashear la nueva contraseña antes de guardarla
            usuario.setContrasenia(passwordEncoder.encode(nuevaContrasenia));
            usuarioRepo.save(usuario);
        } else {
            throw new RuntimeException("Usuario no encontrado para el correo: " + correo);
        }
    }

}
