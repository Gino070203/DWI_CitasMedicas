package com.Citas.Medicas.Autenticacion;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

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
            redirectUrl = "/paciente/dashboard";
        }

        // Redirigimos al usuario a su ruta correspondiente
        response.sendRedirect(redirectUrl);
    }
}
