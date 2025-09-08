package com.uteq.casoslegales.casoslegales.config;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.uteq.casoslegales.casoslegales.Modelo.Acceso;
import com.uteq.casoslegales.casoslegales.Modelo.Usuario;
import com.uteq.casoslegales.casoslegales.Servicio.AccesoServicio;

@Component
public class AccesoFiltro implements Filter {

    @Autowired
    private AccesoServicio accesoService;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);

        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Long accesoId = (Long) session.getAttribute("accesoId");

            if (usuario != null && accesoId != null) {
                Optional<Acceso> accesoOpt = accesoService.obtenerPorId(accesoId);
                if (accesoOpt.isPresent()) {
                    Acceso acceso = accesoOpt.get();
                    // Si fecha de salida ya tiene valor, cerrar sesión
                    if (acceso.getFechaSalida() != null) {
                        session.invalidate();
                        response.sendRedirect(request.getContextPath() + "/login");
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
