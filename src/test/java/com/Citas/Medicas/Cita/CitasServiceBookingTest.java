package com.Citas.Medicas.Cita;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.Citas.Medicas.horarios.Horarios;
import com.Citas.Medicas.horarios.HorariosRepository;
import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Paciente.Paciente;
import com.Citas.Medicas.Paciente.PacienteRepository;
import com.Citas.Medicas.Paciente.PerfilPacienteIncompletoException;
import com.Citas.Medicas.Pago.Pago;
import com.Citas.Medicas.Pago.PagoRepository;
import com.Citas.Medicas.Usuario.Usuario;

class CitasServiceBookingTest {

    private static final Long ID_PACIENTE = 5L;
    private static final Long ID_MEDICO = 8L;
    private static final Long ID_HORARIO = 12L;

    private CitasRepository citasRepository;
    private PacienteRepository pacienteRepository;
    private HorariosRepository horariosRepository;
    private MedicoRepository medicoRepository;
    private PagoRepository pagoRepository;
    private CitasService service;
    private Paciente paciente;
    private Medico medico;
    private Horarios horario;

    @BeforeEach
    void setUp() {
        citasRepository = mock(CitasRepository.class);
        pacienteRepository = mock(PacienteRepository.class);
        horariosRepository = mock(HorariosRepository.class);
        medicoRepository = mock(MedicoRepository.class);
        pagoRepository = mock(PagoRepository.class);
        service = new CitasService(
                citasRepository,
                pacienteRepository,
                medicoRepository,
                horariosRepository,
                pagoRepository,
                30);

        paciente = new Paciente();
        paciente.setIdPaciente(ID_PACIENTE);
        paciente.setEstado(true);
        paciente.setSis("SIS-123");
        paciente.setFechaNacimiento(LocalDate.of(1990, 5, 10));
        paciente.setGenero("FEMENINO");
        Usuario usuarioPaciente = new Usuario();
        usuarioPaciente.setIdUsuario(91L);
        usuarioPaciente.setNombre("Ana");
        usuarioPaciente.setApellido("Pérez");
        usuarioPaciente.setDni("12345678");
        paciente.setUsuario(usuarioPaciente);

        medico = new Medico();
        medico.setIdMedico(ID_MEDICO);
        medico.setEstado(true);

        horario = new Horarios();
        horario.setIdHorario(ID_HORARIO);
        horario.setMedico(medico);
        horario.setDiaSemana("MONDAY");
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(10, 0));
        horario.setEstado(true);

        when(pacienteRepository.findById(ID_PACIENTE))
                .thenReturn(Optional.of(paciente));
        when(horariosRepository.findById(ID_HORARIO)).thenReturn(Optional.of(horario));
        when(medicoRepository.bloquearMedico(ID_MEDICO)).thenReturn(Optional.of(medico));
        when(citasRepository.contarConflictos(
                ID_MEDICO, proximoLunes(), LocalTime.of(9, 30),
                LocalTime.of(10, 0), null)).thenReturn(0L);
        when(citasRepository.save(any(Citas.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(pagoRepository.save(any(Pago.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void pacienteYAdministradorGuardanElMismoTurnoDeTreintaMinutos() {
        LocalDate fecha = proximoLunes();

        Citas desdePaciente = service.reservarPaciente(
                ID_PACIENTE, ID_HORARIO, fecha, LocalTime.of(9, 30));
        Citas desdeAdmin = service.registrar(
                ID_PACIENTE, ID_MEDICO, ID_HORARIO, fecha, LocalTime.of(9, 30));

        assertEquals(LocalTime.of(9, 30), desdePaciente.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), desdePaciente.getHoraFin());
        assertEquals(desdePaciente.getHoraInicio(), desdeAdmin.getHoraInicio());
        assertEquals(desdePaciente.getHoraFin(), desdeAdmin.getHoraFin());
        assertEquals("CONFIRMADA", desdePaciente.getEstado());
        verify(medicoRepository, times(2)).bloquearMedico(ID_MEDICO);
        verify(pagoRepository, times(2)).save(any(Pago.class));

        ArgumentCaptor<Citas> citasGuardadas = ArgumentCaptor.forClass(Citas.class);
        verify(citasRepository, times(2)).save(citasGuardadas.capture());
        assertEquals(List.of(LocalTime.of(10, 0), LocalTime.of(10, 0)),
                citasGuardadas.getAllValues().stream().map(Citas::getHoraFin).toList());
    }

    @Test
    void rechazaUnaHoraQueNoEsInicioDeUnTurnoGenerado() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(ID_PACIENTE, ID_MEDICO, ID_HORARIO,
                        proximoLunes(), LocalTime.of(9, 15)));

        verify(citasRepository, never()).contarConflictos(
                any(), any(), any(), any(), any());
        verify(citasRepository, never()).save(any(Citas.class));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void guardaPagoPendienteConElMismoTurnoSiNoTieneSis() {
        paciente.setSis(null);

        Citas cita = service.reservarPaciente(
                ID_PACIENTE, ID_HORARIO, proximoLunes(), LocalTime.of(9, 0));

        assertEquals(LocalTime.of(9, 0), cita.getHoraInicio());
        assertEquals(LocalTime.of(9, 30), cita.getHoraFin());
        assertEquals("PENDIENTE_PAGO", cita.getEstado());

        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pago.capture());
        assertEquals("PENDIENTE", pago.getValue().getEstado());
    }

    @Test
    void soloLiberaElTurnoCanceladoYNoElRestoDelBloque() {
        Citas reserva = new Citas();
        reserva.setMedico(medico);
        reserva.setFecha(proximoLunes());
        reserva.setHoraInicio(LocalTime.of(9, 0));
        reserva.setHoraFin(LocalTime.of(9, 30));
        reserva.setEstado("CONFIRMADA");
        when(citasRepository.buscarPorMedicoYFecha(ID_MEDICO, proximoLunes()))
                .thenReturn(List.of(reserva));

        List<TurnoCita> turnos = service.obtenerTurnosDisponibles(
                ID_HORARIO, proximoLunes());

        assertEquals(List.of(new TurnoCita(
                LocalTime.of(9, 30), LocalTime.of(10, 0))), turnos);
    }

    @Test
    void exigeCompletarDatosBasicosAntesDeReservar() {
        paciente.setFechaNacimiento(null);

        assertThrows(PerfilPacienteIncompletoException.class,
                () -> service.reservarPaciente(
                        ID_PACIENTE, ID_HORARIO, proximoLunes(), LocalTime.of(9, 0)));

        verify(citasRepository, never()).contarConflictos(
                any(), any(), any(), any(), any());
        verify(citasRepository, never()).save(any(Citas.class));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    private static LocalDate proximoLunes() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }
}
