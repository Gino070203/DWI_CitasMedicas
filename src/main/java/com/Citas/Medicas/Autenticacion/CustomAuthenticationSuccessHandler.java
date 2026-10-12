package com.Citas.Medicas.Autenticacion;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.Citas.Medicas.Paciente.PacienteProvisioningService;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(
            CustomAuthenticationSuccessHandler.class);

    @Autowired
    private PacienteProvisioningService pacienteProvisioningService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // Obtenemos los roles del usuario autenticado
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        String redirectUrl = "/home"; // URL por defecto si no encaja en ninguna

        // Evaluamos a qué panel debe ir según su rol
        if (roles.contains("ROLE_ADMIN")) {
            redirectUrl = "/admin/dashboard";
        } else if (roles.contains("ROLE_MEDICO")) {
            redirectUrl = "/medico/dashboard";
        } else if (roles.contains("ROLE_PACIENTE")) {
            try {
                boolean perfilCompleto = pacienteProvisioningService
                        .asegurarPerfilYComprobarDatos(authentication.getName());
                redirectUrl = perfilCompleto
                        ? "/paciente/dashboard"
                        : "/paciente/perfil?completar=true";
            } catch (RuntimeException e) {
                logger.error("No se pudo preparar el perfil del paciente autenticado", e);
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "No se pudo preparar el perfil de paciente. Inténtalo de nuevo más tarde.");
                return;
            }
        }

        // Redirigimos al usuario a su ruta correspondiente
        response.sendRedirect(redirectUrl);
    }
}
