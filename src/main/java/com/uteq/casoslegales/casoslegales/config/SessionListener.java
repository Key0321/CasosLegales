package com.uteq.casoslegales.casoslegales.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

import com.uteq.casoslegales.casoslegales.Servicio.AccesoServicio;

@Component
public class SessionListener implements HttpSessionListener {

    private final AccesoServicio accesoService;

    public SessionListener(AccesoServicio accesoService) {
        this.accesoService = accesoService;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        Long accesoId = (Long) se.getSession().getAttribute("accesoId");
        if (accesoId != null) {
            accesoService.registrarSalida(accesoId);
        }
    }
}
