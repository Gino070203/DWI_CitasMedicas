package com.Citas.Medicas.Cita;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Citas.Medicas.Horarios.Horarios;
import com.Citas.Medicas.Horarios.HorariosRepository;
import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Pago.PagoRepository;
import com.Citas.Medicas.Paciente.PacienteRepository;

class CitasServiceAvailabilityTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 3, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 3, 31);

    private CitasRepository citasRepository;
    private CitasService citasService;

    @BeforeEach
    void setUp() {
        citasRepository = mock(CitasRepository.class);
        citasService = new CitasService(
                citasRepository,
                mock(PacienteRepository.class),
                mock(MedicoRepository.class),
                mock(HorariosRepository.class),
                mock(PagoRepository.class),
                30);
    }

    @Test
    void generaTurnosDeDuracionFijaSinExcederElBloque() {
        Horarios horario = horario(10L, medico(21L), "MONDAY",
                LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertEquals(List.of(
                new TurnoCita(LocalTime.of(9, 0), LocalTime.of(9, 30)),
                new TurnoCita(LocalTime.of(9, 30), LocalTime.of(10, 0))),
                citasService.generarTurnosDentroDelBloque(horario));

        horario.setHoraInicio(LocalTime.of(9, 5));
        assertEquals(List.of(
                new TurnoCita(LocalTime.of(9, 5), LocalTime.of(9, 35))),
                citasService.generarTurnosDentroDelBloque(horario));
    }

    @Test
    void fechaSigueDisponibleMientrasQuedeAlMenosUnTurnoLibre() {
        Medico medico = medico(21L);
        Horarios horario = horario(11L, medico, "MONDAY",
                LocalTime.of(9, 0), LocalTime.of(10, 0));

        Citas diaCompleto = cita(medico, LocalDate.of(2026, 3, 2),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "CONFIRMADA");
        Citas unTurno = cita(medico, LocalDate.of(2026, 3, 9),
                LocalTime.of(9, 30), LocalTime.of(10, 0), "PENDIENTE_PAGO");
        Citas cancelada = cita(medico, LocalDate.of(2026, 3, 9),
                LocalTime.of(9, 0), LocalTime.of(9, 30), "CANCELADA");
        Citas otroMedico = cita(medico(22L), LocalDate.of(2026, 3, 16),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "CONFIRMADA");

        when(citasRepository.buscarNoCanceladasPorMedicosEnRango(
                List.of(21L), DESDE, HASTA))
                .thenReturn(List.of(diaCompleto, unTurno, cancelada, otroMedico));

        Map<Long, String> disponibles = citasService.obtenerFechasDisponiblesPorHorario(
                List.of(horario), DESDE, HASTA);

        assertEquals(Map.of(11L,
                "2026-03-09,2026-03-16,2026-03-23,2026-03-30"), disponibles);
        verify(citasRepository).buscarNoCanceladasPorMedicosEnRango(
                List.of(21L), DESDE, HASTA);
    }

    @Test
    void devuelveFechasLibresCuandoNoHayCitas() {
        Horarios horario = horario(12L, medico(21L), "MONDAY",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(citasRepository.buscarNoCanceladasPorMedicosEnRango(
                List.of(21L), DESDE, HASTA)).thenReturn(List.of());

        assertEquals(Map.of(12L,
                "2026-03-02,2026-03-09,2026-03-16,2026-03-23,2026-03-30"),
                citasService.obtenerFechasDisponiblesPorHorario(
                        List.of(horario), DESDE, HASTA));
    }

    private static Medico medico(Long id) {
        Medico medico = new Medico();
        medico.setIdMedico(id);
        medico.setEstado(true);
        return medico;
    }

    private static Horarios horario(Long id, Medico medico, String dia,
                                    LocalTime inicio, LocalTime fin) {
        Horarios horario = new Horarios();
        horario.setIdHorario(id);
        horario.setMedico(medico);
        horario.setDiaSemana(dia);
        horario.setHoraInicio(inicio);
        horario.setHoraFin(fin);
        horario.setEstado(true);
        return horario;
    }

    private static Citas cita(Medico medico, LocalDate fecha,
                              LocalTime inicio, LocalTime fin, String estado) {
        Citas cita = new Citas();
        cita.setMedico(medico);
        cita.setFecha(fecha);
        cita.setHoraInicio(inicio);
        cita.setHoraFin(fin);
        cita.setEstado(estado);
        return cita;
    }
}
