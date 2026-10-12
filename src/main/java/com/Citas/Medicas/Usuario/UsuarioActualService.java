package com.Citas.Medicas.Usuario;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> buscarUsuarioActual(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        // Tanto el login tradicional como Google exponen el correo mediante
        // Authentication.getName() en la configuración actual de seguridad.
        String correo = authentication.getName();
        if (correo == null || correo.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByCorreo(correo.trim());
    }
}
