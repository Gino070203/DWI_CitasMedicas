package com.Citas.Medicas.Autenticacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.Citas.Medicas.Paciente.PacienteProvisioningService;

class CustomAuthenticationSuccessHandlerTest {

    private PacienteProvisioningService provisioning;
    private CustomAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        provisioning = mock(PacienteProvisioningService.class);
        handler = new CustomAuthenticationSuccessHandler();
        ReflectionTestUtils.setField(handler, "pacienteProvisioningService", provisioning);
    }

    @Test
    void dirigeAlPanelCuandoElPerfilPacienteEstaCompleto() throws Exception {
        when(provisioning.asegurarPerfilYComprobarDatos("ana@example.com"))
                .thenReturn(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                pacienteAutenticado("ana@example.com"));

        assertEquals("/paciente/dashboard", response.getRedirectedUrl());
        verify(provisioning).asegurarPerfilYComprobarDatos("ana@example.com");
    }

    @Test
    void dirigeAlOnboardingCuandoProvisionaUnPerfilIncompleto() throws Exception {
        when(provisioning.asegurarPerfilYComprobarDatos("ana@example.com"))
                .thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                pacienteAutenticado("ana@example.com"));

        assertEquals("/paciente/perfil?completar=true", response.getRedirectedUrl());
    }

    private static UsernamePasswordAuthenticationToken pacienteAutenticado(String correo) {
        return new UsernamePasswordAuthenticationToken(
                correo,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_PACIENTE")));
    }
}
