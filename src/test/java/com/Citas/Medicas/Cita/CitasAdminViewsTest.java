package com.Citas.Medicas.Cita;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.Citas.Medicas.horarios.HorariosRepository;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Paciente.PacienteRepository;

@SpringBootTest
class CitasAdminViewsTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private CitasService citasService;

    @MockitoBean
    private MedicoRepository medicoRepository;

    @MockitoBean
    private PacienteRepository pacienteRepository;

    @MockitoBean
    private HorariosRepository horariosRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        when(citasService.getDuracionConsultaMinutos()).thenReturn(30);
        when(medicoRepository.listarMedicosActivos()).thenReturn(List.of());
        when(pacienteRepository.listarActivos()).thenReturn(List.of());
        when(horariosRepository.listarHorariosActivos()).thenReturn(List.of());
    }

    @Test
    @WithMockUser(username = "admin@example.invalid", roles = "ADMIN")
    void muestraTurnoReservableYNoPermiteEditarLaHoraFinal() throws Exception {
        String html = mockMvc.perform(get("/admin/citas/nuevo"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("Turno libre"));
        assertTrue(html.contains("name=\"horaInicio\""));
        assertTrue(!html.contains("name=\"horaFin\""));
        assertTrue(html.contains("data-turnos-url=\"/admin/citas/turnos\""));
    }

    @Test
    @WithMockUser(username = "admin@example.invalid", roles = "ADMIN")
    void endpointAdministrativoDevuelveIntervaloDisponibleEnJson() throws Exception {
        when(citasService.obtenerTurnosDisponibles(
                12L, LocalDate.of(2026, 10, 19)))
                .thenReturn(List.of(new TurnoCita(
                        LocalTime.of(9, 0), LocalTime.of(9, 30))));

        String json = mockMvc.perform(get("/admin/citas/turnos")
                        .param("idHorario", "12")
                        .param("fecha", "2026-10-19"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(json.contains("\"horaInicio\":\"09:00\""));
        assertTrue(json.contains("\"horaFin\":\"09:30\""));
    }

    @Test
    @WithMockUser(username = "paciente@example.invalid", roles = "PACIENTE")
    void endpointAdministrativoNoEsAccesibleParaPaciente() throws Exception {
        mockMvc.perform(get("/admin/citas/turnos")
                        .param("idHorario", "12")
                        .param("fecha", "2026-10-19"))
                .andExpect(status().isForbidden());
    }
}
