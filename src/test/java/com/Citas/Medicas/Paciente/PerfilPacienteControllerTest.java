package com.Citas.Medicas.Paciente;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioActualService;

@SpringBootTest
class PerfilPacienteControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private UsuarioActualService usuarioActualService;

    @MockitoBean
    private PerfilPacienteService perfilPacienteService;

    @MockitoBean
    private PacienteProvisioningService pacienteProvisioningService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        when(usuarioActualService.buscarUsuarioActual(any(Authentication.class)))
                .thenReturn(Optional.empty());
        when(pacienteProvisioningService.asegurarPerfilPaciente(anyLong()))
                .thenReturn(new Paciente());
    }

    @Test
    @WithMockUser(username = "paciente@example.invalid", roles = "PACIENTE")
    void muestraElPerfilPropioYDejaIdentificadoresMedicosEnSoloLectura() throws Exception {
        Usuario usuario = usuarioActual();
        when(usuarioActualService.buscarUsuarioActual(any(Authentication.class)))
                .thenReturn(Optional.of(usuario));
        when(perfilPacienteService.obtenerPerfil(52L)).thenReturn(vistaPerfil());

        String html = mockMvc.perform(get("/paciente/perfil"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("Mi perfil"));
        assertTrue(html.contains("ana@example.com"));
        assertTrue(html.contains("name=\"dni\""));
        assertTrue(html.contains("name=\"fechaNacimiento\""));
        assertTrue(html.contains("HC-1042"));
        assertTrue(html.contains("name=\"sis\"") == false);
        assertTrue(html.contains("name=\"numeroHistoria\"") == false);
        assertTrue(html.contains("/paciente/perfil"));
    }

    @Test
    @WithMockUser(username = "paciente@example.invalid", roles = "PACIENTE")
    void explicaAlPacienteQueDebeCompletarSuPerfilAntesDeReservar() throws Exception {
        when(usuarioActualService.buscarUsuarioActual(any(Authentication.class)))
                .thenReturn(Optional.of(usuarioActual()));
        when(perfilPacienteService.obtenerPerfil(52L)).thenReturn(vistaPerfil());

        String html = mockMvc.perform(get("/paciente/perfil")
                        .param("completar", "true"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("Completa tus datos personales antes de reservar una cita."));
    }

    @Test
    @WithMockUser(username = "paciente@example.invalid", roles = "PACIENTE")
    void validaLosCamposAntesDeLlamarAlServicioDeActualizacion() throws Exception {
        Usuario usuario = usuarioActual();
        when(usuarioActualService.buscarUsuarioActual(any(Authentication.class)))
                .thenReturn(Optional.of(usuario));
        when(perfilPacienteService.obtenerPerfil(52L)).thenReturn(vistaPerfil());

        String html = mockMvc.perform(post("/paciente/perfil").with(csrf())
                        .param("nombre", "")
                        .param("apellido", "Perez")
                        .param("dni", "123")
                        .param("telefono", "1------2")
                        .param("direccion", "Calle 1")
                        .param("fechaNacimiento", "2099-01-01")
                        .param("genero", "INVALIDO"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("Ingresa tu nombre."));
        assertTrue(html.contains("exactamente 8 dígitos"));
        assertTrue(html.contains("teléfono válido"));
        assertTrue(html.contains("no puede ser futura"));
        verify(perfilPacienteService, never())
                .actualizarPerfil(any(), any(PerfilPacienteForm.class));
    }

    @Test
    @WithMockUser(username = "paciente@example.invalid", roles = "PACIENTE")
    void actualizaSoloLaCuentaAutenticadaAunqueElPostIncluyaOtroId() throws Exception {
        Usuario usuario = usuarioActual();
        when(usuarioActualService.buscarUsuarioActual(any(Authentication.class)))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/paciente/perfil").with(csrf())
                        .param("idUsuario", "999")
                        .param("nombre", "Ana María")
                        .param("apellido", "Pérez-Ríos")
                        .param("dni", "12345678")
                        .param("telefono", "+51 987 654 321")
                        .param("direccion", "Av. Salud 123")
                        .param("fechaNacimiento", "1995-04-15")
                        .param("genero", "FEMENINO"))
                .andExpect(status().is3xxRedirection());

        verify(perfilPacienteService).actualizarPerfil(
                org.mockito.ArgumentMatchers.eq(52L), any(PerfilPacienteForm.class));
    }

    @Test
    @WithMockUser(username = "medico@example.invalid", roles = "MEDICO")
    void bloqueaElPerfilParaOtrosRoles() throws Exception {
        mockMvc.perform(get("/paciente/perfil"))
                .andExpect(status().isForbidden());
    }

    private static Usuario usuarioActual() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(52L);
        usuario.setNombre("Ana");
        return usuario;
    }

    private static PerfilPacienteVista vistaPerfil() {
        PerfilPacienteForm formulario = new PerfilPacienteForm();
        formulario.setNombre("Ana");
        formulario.setApellido("Pérez");
        formulario.setDni("12345678");
        formulario.setTelefono("+51 987 654 321");
        formulario.setDireccion("Av. Salud 123");
        formulario.setFechaNacimiento(LocalDate.of(1995, 4, 15));
        formulario.setGenero("FEMENINO");
        return new PerfilPacienteVista(formulario, "ana@example.com", "HC-1042", "SIS-12345");
    }
}
