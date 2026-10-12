package com.Citas.Medicas.Paciente;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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

import com.Citas.Medicas.Cita.Citas;
import com.Citas.Medicas.Cita.CitasService;
import com.Citas.Medicas.Cita.TurnoCita;
import com.Citas.Medicas.Especialidad.Especialidad;
import com.Citas.Medicas.Especialidad.EspecialidadRepository;
import com.Citas.Medicas.horarios.Horarios;
import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioActualService;

@SpringBootTest
class PacienteViewsTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private UsuarioActualService usuarioActualService;

    @MockitoBean
    private PacienteProvisioningService pacienteProvisioningService;

    @MockitoBean
    private PacienteService pacienteService;

    @MockitoBean
    private EspecialidadRepository especialidadRepository;

    @MockitoBean
    private CitasService citasService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        when(usuarioActualService.buscarUsuarioActual(any(Authentication.class)))
                .thenReturn(Optional.empty());
        when(pacienteProvisioningService.asegurarPerfilPaciente(any()))
                .thenReturn(new Paciente());
        when(citasService.obtenerFechasDisponiblesPorHorario(
                anyList(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Map.of());
        when(citasService.getDuracionConsultaMinutos()).thenReturn(30);
    }

    @Test
    @WithMockUser(username = "ui-paciente@example.invalid", roles = "PACIENTE")
    void renderizaDashboardConNavegacionCompartidaSinIdentificadores() throws Exception {
        String html = mockMvc.perform(get("/paciente/dashboard"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(html.contains("Hola,"));
        assertTrue(html.contains("Reservar una cita"));
        assertTrue(html.contains("Mis citas e historial"));
        assertTrue(html.contains("Cerrar sesión"));
        assertTrue(html.contains("fonts.googleapis.com/css2?family=Manrope"));
        assertTrue(!html.contains("Número de Historia"));
        assertTrue(!html.contains("DNI:"));
    }

    @Test
    @WithMockUser(username = "ui-paciente@example.invalid", roles = "PACIENTE")
    void renderizaHistorialVacioConAccionParaReservar() throws Exception {
        String html = mockMvc.perform(get("/paciente/citas"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(html.contains("Aún no tienes citas"));
        assertTrue(html.contains("Buscar un horario"));
    }

    @Test
    @WithMockUser(username = "ui-paciente@example.invalid", roles = "PACIENTE")
    void renderizaReservaConEspecialidadYEstadoInicial() throws Exception {
        String html = mockMvc.perform(get("/paciente/reservar"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(html.contains("Ver disponibilidad"));
        assertTrue(html.contains("Encuentra el horario adecuado"));
        assertTrue(!html.contains("MONDAY"));
    }

    @Test
    @WithMockUser(username = "ui-paciente@example.invalid", roles = "PACIENTE")
    void muestraNombreDiaEnEspanolEnTarjetaDeHorario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(81L);
        usuario.setNombre("Ana");
        when(usuarioActualService.buscarUsuarioActual(any(Authentication.class)))
                .thenReturn(Optional.of(usuario));

        Especialidad especialidad = mock(Especialidad.class);
        when(especialidad.getNombre()).thenReturn("Cardiología");
        when(especialidadRepository.findAll()).thenReturn(List.of(especialidad));

        Usuario medicoUsuario = new Usuario();
        medicoUsuario.setNombre("Luis");
        medicoUsuario.setApellido("Pérez");
        Medico medico = mock(Medico.class);
        when(medico.getIdMedico()).thenReturn(21L);
        when(medico.getUsuario()).thenReturn(medicoUsuario);
        when(medico.getEspecialidad()).thenReturn(especialidad);

        Horarios horario = new Horarios();
        horario.setIdHorario(14L);
        horario.setMedico(medico);
        horario.setDiaSemana("MONDAY");
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(10, 0));
        when(pacienteService.obtenerHorariosPorEspecialidad(2L))
                .thenReturn(List.of(horario));
        when(citasService.obtenerFechasDisponiblesPorHorario(
                anyList(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Map.of(14L, "2026-10-19"));

        String html = mockMvc.perform(get("/paciente/reservar")
                        .param("idEspecialidad", "2"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(html.contains("Lunes"));
        assertTrue(html.contains("data-fechas-disponibles=\"2026-10-19\""));
        assertTrue(html.contains("data-id-horario=\"14\""));
        assertTrue(html.contains("reserva-disponibilidad.js"));
        assertTrue(!html.contains(">MONDAY<"));
        assertTrue(html.contains("Ana"));
        assertTrue(html.contains("Reservar este turno"));
    }

    @Test
    @WithMockUser(username = "ui-paciente@example.invalid", roles = "PACIENTE")
    void renderizaDatosDeCitaEnTarjetaDeHistorial() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(81L);
        usuario.setNombre("Ana");
        when(usuarioActualService.buscarUsuarioActual(any(Authentication.class)))
                .thenReturn(Optional.of(usuario));

        Paciente perfilPaciente = mock(Paciente.class);
        when(perfilPaciente.getIdPaciente()).thenReturn(19L);
        when(pacienteService.obtenerPacientePorUsuarioId(81L)).thenReturn(perfilPaciente);

        Especialidad especialidad = mock(Especialidad.class);
        when(especialidad.getNombre()).thenReturn("Cardiología");
        Usuario medicoUsuario = new Usuario();
        medicoUsuario.setNombre("Luis");
        medicoUsuario.setApellido("Pérez");
        Medico medico = mock(Medico.class);
        when(medico.getUsuario()).thenReturn(medicoUsuario);
        when(medico.getEspecialidad()).thenReturn(especialidad);

        Horarios horario = new Horarios();
        horario.setDiaSemana("WEDNESDAY");

        Citas cita = new Citas();
        cita.setFecha(LocalDate.of(2026, 4, 15));
        cita.setHoraInicio(LocalTime.of(9, 0));
        cita.setHoraFin(LocalTime.of(10, 0));
        cita.setEstado("CONFIRMADA");
        cita.setMedico(medico);
        cita.setHorario(horario);
        when(pacienteService.obtenerHistorialCitas(19L)).thenReturn(List.of(cita));

        String html = mockMvc.perform(get("/paciente/citas"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(html.contains("15/04/2026"));
        assertTrue(html.contains("Confirmada"));
        assertTrue(html.contains("Miércoles"));
        assertTrue(html.contains("Luis Pérez"));
    }

    @Test
    @WithMockUser(username = "ui-paciente@example.invalid", roles = "PACIENTE")
    void apiPacienteDevuelveElIntervaloReservableCalculado() throws Exception {
        when(citasService.obtenerTurnosDisponibles(
                14L, LocalDate.of(2026, 10, 19)))
                .thenReturn(List.of(new TurnoCita(
                        LocalTime.of(9, 30), LocalTime.of(10, 0))));

        String json = mockMvc.perform(get("/paciente/reservar/turnos")
                        .param("idHorario", "14")
                        .param("fecha", "2026-10-19"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(json.contains("\"horaInicio\":\"09:30\""));
        assertTrue(json.contains("\"horaFin\":\"10:00\""));
    }

    @Test
    @WithMockUser(username = "admin@example.invalid", roles = "ADMIN")
    void apiAdministrativaDevuelveElMismoIntervaloReservable() throws Exception {
        when(citasService.obtenerTurnosDisponibles(
                14L, LocalDate.of(2026, 10, 19)))
                .thenReturn(List.of(new TurnoCita(
                        LocalTime.of(9, 30), LocalTime.of(10, 0))));

        String json = mockMvc.perform(get("/admin/citas/turnos")
                        .param("idHorario", "14")
                        .param("fecha", "2026-10-19"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(json.contains("\"horaInicio\":\"09:30\""));
        assertTrue(json.contains("\"horaFin\":\"10:00\""));
    }
}
