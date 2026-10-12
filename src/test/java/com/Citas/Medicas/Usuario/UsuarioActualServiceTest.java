package com.Citas.Medicas.Usuario;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

class UsuarioActualServiceTest {

    private static final String CORREO = "paciente@example.com";

    private UsuarioRepository usuarioRepository;
    private UsuarioActualService usuarioActualService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioActualService = new UsuarioActualService(usuarioRepository);
    }

    @Test
    void buscaUsuarioDeAutenticacionTradicionalPorCorreo() {
        Usuario usuario = new Usuario();
        UserDetails principal = User.withUsername(CORREO)
                .password("irrelevante")
                .authorities("ROLE_PACIENTE")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioActualService.buscarUsuarioActual(authentication);

        assertSame(usuario, resultado.orElseThrow());
        verify(usuarioRepository).findByCorreo(CORREO);
    }

    @Test
    void buscaUsuarioDeGooglePorAtributoEmail() {
        Usuario usuario = new Usuario();
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_PACIENTE")),
                Map.of("email", CORREO),
                "email");
        Authentication authentication = new OAuth2AuthenticationToken(
                principal, principal.getAuthorities(), "google");
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioActualService.buscarUsuarioActual(authentication);

        assertSame(usuario, resultado.orElseThrow());
        verify(usuarioRepository).findByCorreo(CORREO);
    }

    @Test
    void noBuscaUsuarioParaSesionAnonima() {
        Authentication authentication = new AnonymousAuthenticationToken(
                "test-key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        Optional<Usuario> resultado = usuarioActualService.buscarUsuarioActual(authentication);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(usuarioRepository);
    }
}
