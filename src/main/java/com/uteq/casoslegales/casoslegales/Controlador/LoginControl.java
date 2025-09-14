package com.uteq.casoslegales.casoslegales.Controlador;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;

import com.uteq.casoslegales.casoslegales.Modelo.Acceso;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.AccesoServicio;
import com.uteq.casoslegales.casoslegales.Servicio.UsuarioServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.MessagingException;

import java.util.UUID;

@Controller
public class LoginControl {

    @Autowired
    private UsuarioServicio usuarioService;

    @Autowired
    private AccesoServicio accesoService;

    // Mapa temporal para tokens de verificación (correo -> código)
    private Map<String, String> codigosVerificacion = new ConcurrentHashMap<>();

    // Mapa temporal para tokens de reset (correo -> token)
    private Map<String, String> tokensReset = new ConcurrentHashMap<>();

    @GetMapping("/login")
    public String mostrarFormularioLogin() {
        return "login"; // login.html con Vue
    }

    // Verificar credenciales y enviar código de verificación
    @PostMapping("/api/login/verificar-credenciales")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarCredenciales(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");
        String contrasenia = request.get("contrasenia");

        Optional<Usuario> usuario = usuarioService.autenticar(correo, contrasenia);
        Map<String, Object> response = new HashMap<>();

        if (usuario.isPresent()) {
            String codigo = generarCodigo();
            codigosVerificacion.put(correo, codigo);

            try {
                enviarCorreoVerificacion(correo, codigo);
                response.put("success", true);
            } catch (MessagingException e) {
                response.put("success", false);
                response.put("message", "Error al enviar el código de verificación");
            }
        } else {
            response.put("success", false);
            response.put("message", "Correo o contraseña incorrectos");
        }
        return ResponseEntity.ok(response);
    }

    // Verificar código y completar login
    @PostMapping("/api/login/verificar-codigo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarCodigo(@RequestBody Map<String, String> request,
                                                              HttpSession session,
                                                              HttpServletRequest httpRequest) {
        String correo = request.get("correo");
        String codigo = request.get("codigo");

        Map<String, Object> response = new HashMap<>();
        String codigoAlmacenado = codigosVerificacion.get(correo);

        if (codigoAlmacenado != null && codigoAlmacenado.equals(codigo)) {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorCorreo(correo);
            if (usuarioOpt.isPresent()) {
                Usuario user = usuarioOpt.get();
                session.setAttribute("usuario", user);
                session.setAttribute("rol", user.getRol().getNombre());

                Acceso acceso = new Acceso();
                acceso.setUsuario(user);
                acceso.setFechaIngreso(LocalDateTime.now());
                acceso.setIpOrigen(httpRequest.getRemoteAddr());
                acceso.setCreadoPor(user);
                acceso.setFechaCreacion(LocalDateTime.now());
                accesoService.guardar(acceso);

                session.setAttribute("accesoId", acceso.getId());

                codigosVerificacion.remove(correo);

                String redirectUrl;
                switch (user.getRol().getNombre().toLowerCase()) {
                    case "abogado":
                        redirectUrl = "/abogado/inicio";
                        break;
                    case "cliente":
                        redirectUrl = "/cliente/inicio";
                        break;
                    case "admin":
                        redirectUrl = "/admin/gestion_procesos_legales";
                        break;
                    default:
                        response.put("success", false);
                        response.put("message", "Rol no reconocido");
                        return ResponseEntity.ok(response);
                }
                response.put("success", true);
                response.put("redirectUrl", redirectUrl);
            } else {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
            }
        } else {
            response.put("success", false);
            response.put("message", "Código de verificación incorrecto");
        }
        return ResponseEntity.ok(response);
    }

    // Iniciar reset de contraseña (enviar enlace)
    @PostMapping("/api/login/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");

        Optional<Usuario> usuario = usuarioService.buscarPorCorreo(correo);
        Map<String, Object> response = new HashMap<>();

        if (usuario.isPresent()) {
            String token = UUID.randomUUID().toString();
            tokensReset.put(token, correo);

            try {
                enviarCorreoReset(correo, token);
                response.put("success", true);
                response.put("message", "Enlace de restablecimiento enviado a tu correo");
            } catch (MessagingException e) {
                response.put("success", false);
                response.put("message", "Error al enviar el enlace de restablecimiento");
            }
        } else {
            response.put("success", false);
            response.put("message", "Correo no registrado");
        }
        return ResponseEntity.ok(response);
    }

    // Verificar token
    @GetMapping("/api/login/verificar-token")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarToken(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        String correo = tokensReset.get(token);

        if (correo != null) {
            response.put("success", true);
            response.put("correo", correo);
        } else {
            response.put("success", false);
            response.put("message", "Enlace inválido o expirado");
        }
        return ResponseEntity.ok(response);
    }

    // Confirmar reset de contraseña
    @PostMapping("/api/login/confirm-reset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmReset(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String nuevaContrasenia = request.get("nuevaContrasenia");

        Map<String, Object> response = new HashMap<>();
        String correo = tokensReset.get(token);

        if (correo != null) {
            try {
                usuarioService.actualizarContrasenia(correo, nuevaContrasenia);
                tokensReset.remove(token);
                response.put("success", true);
                response.put("message", "Contraseña actualizada exitosamente");
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Error al actualizar la contraseña");
            }
        } else {
            response.put("success", false);
            response.put("message", "Enlace inválido o expirado");
        }
        return ResponseEntity.ok(response);
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        Long accesoId = (Long) session.getAttribute("accesoId");

        if (accesoId != null) {
            accesoService.registrarSalida(accesoId);
        }

        session.invalidate();
        return "redirect:/login";
    }

    // Generar código de 6 dígitos
    private String generarCodigo() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    // Enviar correo de verificación
    private void enviarCorreoVerificacion(String correoDestino, String codigo) throws MessagingException {
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

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(remitente));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
        message.setSubject("Código de Verificación para Inicio de Sesión");
        message.setText("Hola,\n\nTu código de verificación es: " + codigo + "\n\nÚsalo para completar el inicio de sesión.\n\nSaludos,\nEl equipo de soporte");
        Transport.send(message);
    }

    // Enviar correo de reset con enlace
    private void enviarCorreoReset(String correoDestino, String token) throws MessagingException {
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

        String resetLink = "http://localhost:8080/login?token=" + token; // Ajusta la URL según tu entorno

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(remitente));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
        message.setSubject("Restablecer Contraseña");
        message.setText("Hola,\n\nHaz clic en el siguiente enlace para restablecer tu contraseña:\n\n" + resetLink + "\n\nEste enlace es válido por 10 minutos.\n\nSaludos,\nEl equipo de soporte");
        Transport.send(message);
    }
}