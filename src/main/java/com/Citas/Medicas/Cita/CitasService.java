
package com.Citas.Medicas.Cita;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.horarios.Horarios;
import com.Citas.Medicas.horarios.HorariosRepository;
import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Paciente.Paciente;
import com.Citas.Medicas.Paciente.PacienteProvisioningService;
import com.Citas.Medicas.Paciente.PacienteRepository;
import com.Citas.Medicas.Paciente.PerfilPacienteIncompletoException;
import com.Citas.Medicas.Pago.Pago;
import com.Citas.Medicas.Pago.PagoRepository;

@Service
public class CitasService {

    private final CitasRepository citasRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final HorariosRepository horariosRepository;
    private final PagoRepository pagoRepository;
    private final int duracionConsultaMinutos;

    public CitasService(
            CitasRepository citasRepository,
            PacienteRepository pacienteRepository,
            MedicoRepository medicoRepository,
            HorariosRepository horariosRepository,
            PagoRepository pagoRepository,
            @Value("${agenda.duracion-consulta-minutos:30}") int duracionConsultaMinutos) {

        if (duracionConsultaMinutos <= 0) {
            throw new IllegalArgumentException(
                    "La duración de consulta debe ser mayor que cero.");
        }

        this.citasRepository = citasRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.horariosRepository = horariosRepository;
        this.pagoRepository = pagoRepository;
        this.duracionConsultaMinutos = duracionConsultaMinutos;
    }

    public int getDuracionConsultaMinutos() {
        return duracionConsultaMinutos;
    }

    // LISTAR TODAS LAS CITAS
    @Transactional(readOnly = true)
    public List<Citas> listar() {
        return citasRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Citas> filtrar(String texto, String estado,
                               LocalDate fecha, Long idMedico) {
        String criterio = texto == null || texto.isBlank() ? null : texto.trim();
        String estadoLimpio = estado == null || estado.isBlank() ? null : estado.trim();
        Long idCita = null;
        if (criterio != null && criterio.matches("[0-9]+")) {
            try {
                idCita = Long.valueOf(criterio);
            } catch (NumberFormatException ignored) {
                // Se conserva la búsqueda textual si el número excede Long.
            }
        }
        return citasRepository.filtrarCitas(
                criterio, idCita, estadoLimpio, fecha, idMedico);
    }

    // BUSCAR CITA POR ID
    @Transactional(readOnly = true)
    public Citas obtener(Long idCita) {
        return citasRepository.findById(idCita)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La cita no existe."
                        ));
    }

    /** Genera turnos consecutivos del tamaño configurado dentro del bloque. */
    public List<TurnoCita> generarTurnosDentroDelBloque(Horarios horario) {
        if (horario == null || horario.getHoraInicio() == null
                || horario.getHoraFin() == null
                || !horario.getHoraInicio().isBefore(horario.getHoraFin())) {
            return List.of();
        }

        long minutosBloque = Duration.between(
                horario.getHoraInicio(), horario.getHoraFin()).toMinutes();
        if (minutosBloque < duracionConsultaMinutos) {
            return List.of();
        }

        List<TurnoCita> turnos = new ArrayList<>();
        for (long desplazamiento = 0;
             desplazamiento + duracionConsultaMinutos <= minutosBloque;
             desplazamiento += duracionConsultaMinutos) {
            LocalTime inicio = horario.getHoraInicio().plusMinutes(desplazamiento);
            turnos.add(new TurnoCita(
                    inicio, inicio.plusMinutes(duracionConsultaMinutos)));
        }
        return List.copyOf(turnos);
    }

    /** Devuelve las fechas en las que queda al menos un turno libre por bloque. */
    @Transactional(readOnly = true)
    public Map<Long, String> obtenerFechasDisponiblesPorHorario(
            List<Horarios> horarios, LocalDate desde, LocalDate hasta) {

        if (horarios == null || horarios.isEmpty()
                || desde == null || hasta == null || hasta.isBefore(desde)) {
            return Map.of();
        }

        List<Long> idsMedico = horarios.stream()
                .filter(this::horarioTieneMedico)
                .map(h -> h.getMedico().getIdMedico())
                .distinct()
                .toList();
        if (idsMedico.isEmpty()) {
            return Map.of();
        }

        List<Citas> citasEnRango = citasRepository
                .buscarNoCanceladasPorMedicosEnRango(idsMedico, desde, hasta);
        Map<Long, List<Citas>> citasPorMedico = citasEnRango.stream()
                .filter(c -> c.getMedico() != null
                        && c.getMedico().getIdMedico() != null)
                .filter(this::citaBloqueaTurno)
                .collect(Collectors.groupingBy(c -> c.getMedico().getIdMedico()));

        Map<Long, String> fechasDisponibles = new LinkedHashMap<>();
        for (Horarios horario : horarios) {
            if (horario == null || horario.getIdHorario() == null) {
                continue;
            }

            Set<LocalDate> libres = new TreeSet<>();
            List<TurnoCita> turnosDelBloque = generarTurnosDentroDelBloque(horario);
            if (horarioEstaActivo(horario) && !turnosDelBloque.isEmpty()) {
                List<Citas> citasDelMedico = citasPorMedico.getOrDefault(
                        horario.getMedico().getIdMedico(), List.of());
                for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
                    if (!fecha.getDayOfWeek().name()
                            .equalsIgnoreCase(horario.getDiaSemana())) {
                        continue;
                    }
                    LocalDate fechaEvaluada = fecha;
                    boolean quedaTurno = turnosDelBloque.stream().anyMatch(turno ->
                            citasDelMedico.stream().noneMatch(cita ->
                                    ocupaTurno(cita, fechaEvaluada, turno)));
                    if (quedaTurno) {
                        libres.add(fecha);
                    }
                }
            }
            fechasDisponibles.put(horario.getIdHorario(), libres.stream()
                    .map(LocalDate::toString).collect(Collectors.joining(",")));
        }
        return fechasDisponibles;
    }

    @Transactional(readOnly = true)
    public List<TurnoCita> obtenerTurnosDisponibles(Long idHorario, LocalDate fecha) {
        Horarios horario = horariosRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado."));
        validarFechaYHorario(horario, fecha);

        List<Citas> citasDelDia = citasRepository.buscarPorMedicoYFecha(
                horario.getMedico().getIdMedico(), fecha).stream()
                .filter(this::citaBloqueaTurno)
                .toList();

        return generarTurnosDentroDelBloque(horario).stream()
                .filter(turno -> citasDelDia.stream()
                        .noneMatch(cita -> ocupaTurno(cita, fecha, turno)))
                .toList();
    }

    private boolean horarioTieneMedico(Horarios horario) {
        return horario != null && horario.getMedico() != null
                && horario.getMedico().getIdMedico() != null;
    }

    private boolean horarioEstaActivo(Horarios horario) {
        return Boolean.TRUE.equals(horario.getEstado())
                && horarioTieneMedico(horario)
                && Boolean.TRUE.equals(horario.getMedico().getEstado())
                && horario.getDiaSemana() != null;
    }

    private boolean citaBloqueaTurno(Citas cita) {
        return cita != null && (cita.getEstado() == null
                || !"CANCELADA".equalsIgnoreCase(cita.getEstado()))
                && cita.getFecha() != null
                && cita.getHoraInicio() != null
                && cita.getHoraFin() != null;
    }

    private boolean ocupaTurno(Citas cita, LocalDate fecha, TurnoCita turno) {
        return citaBloqueaTurno(cita)
                && cita.getFecha().equals(fecha)
                && cita.getMedico() != null
                && cita.getMedico().getIdMedico() != null
                && turno.horaInicio().isBefore(cita.getHoraFin())
                && turno.horaFin().isAfter(cita.getHoraInicio());
    }

    private void validarFechaYHorario(Horarios horario, LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        if (fecha == null || fecha.isBefore(hoy) || fecha.isAfter(hoy.plusDays(90))) {
            throw new IllegalArgumentException(
                    "Selecciona una fecha dentro de los próximos 90 días.");
        }
        if (!horarioEstaActivo(horario)) {
            throw new IllegalArgumentException("El bloque de atención no está activo.");
        }
        if (!fecha.getDayOfWeek().name().equalsIgnoreCase(horario.getDiaSemana())) {
            throw new IllegalArgumentException(
                    "La fecha no corresponde al día de atención del médico.");
        }
    }

    /** Reserva un turno seleccionado desde el portal del paciente. */
    @Transactional
    public Citas reservarPaciente(Long idPaciente, Long idHorario,
                                  LocalDate fecha, LocalTime horaInicio) {
        return registrarEnTurno(idPaciente, null, idHorario, fecha, horaInicio);
    }

    /** Reserva el mismo tipo de turno desde el flujo administrativo. */
    @Transactional
    public Citas registrar(Long idPaciente, Long idMedico, Long idHorario,
                           LocalDate fecha, LocalTime horaInicio) {
        return registrarEnTurno(idPaciente, idMedico, idHorario, fecha, horaInicio);
    }

    private Citas registrarEnTurno(Long idPaciente, Long idMedicoEsperado,
                                   Long idHorario, LocalDate fecha,
                                   LocalTime horaInicio) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new IllegalArgumentException("El paciente no existe."));
        Horarios horario = horariosRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("El horario no existe."));

        if (!horarioEstaActivo(horario)) {
            throw new IllegalArgumentException("El bloque de atención no está activo.");
        }
        Long idMedico = horario.getMedico().getIdMedico();
        if (idMedicoEsperado != null && !idMedicoEsperado.equals(idMedico)) {
            throw new IllegalArgumentException(
                    "El bloque seleccionado no pertenece al médico elegido.");
        }

        LocalDate hoy = LocalDate.now();
        if (fecha == null || fecha.isBefore(hoy) || fecha.isAfter(hoy.plusDays(90))) {
            throw new IllegalArgumentException(
                    "Selecciona una fecha dentro de los próximos 90 días.");
        }
        if (!fecha.getDayOfWeek().name().equalsIgnoreCase(horario.getDiaSemana())) {
            throw new IllegalArgumentException(
                    "La fecha no corresponde al día de atención del médico.");
        }
        if (horaInicio == null) {
            throw new IllegalArgumentException("Selecciona un turno disponible.");
        }
        if (!Boolean.TRUE.equals(paciente.getEstado())) {
            throw new IllegalArgumentException("El paciente está inactivo.");
        }
        if (!PacienteProvisioningService.datosBasicosCompletos(paciente)) {
            throw new PerfilPacienteIncompletoException();
        }

        // Bloquear el médico serializa las reservas de ambas vistas.
        Medico medico = medicoRepository.bloquearMedico(idMedico)
                .orElseThrow(() -> new IllegalArgumentException("El médico no existe."));
        if (!Boolean.TRUE.equals(medico.getEstado())) {
            throw new IllegalArgumentException("El médico está inactivo.");
        }

        TurnoCita turno = generarTurnosDentroDelBloque(horario).stream()
                .filter(candidato -> candidato.horaInicio().equals(horaInicio))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El turno ya no es válido para ese bloque."));

        if (citasRepository.contarConflictos(
                idMedico, fecha, turno.horaInicio(), turno.horaFin(), null) > 0) {
            throw new IllegalArgumentException(
                    "Ese turno acaba de ser reservado. Selecciona otro horario.");
        }

        Citas cita = new Citas();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setHorario(horario);
        cita.setFecha(fecha);
        cita.setHoraInicio(turno.horaInicio());
        cita.setHoraFin(turno.horaFin());

        boolean tieneSis = paciente.getSis() != null
                && !paciente.getSis().isBlank()
                // Los registros antiguos con estado nulo se consideran validados;
                // un SIS ingresado por el paciente queda explícitamente en false.
                && !Boolean.FALSE.equals(paciente.getSisVerificado());
        Pago pago = new Pago();
        pago.setCita(cita);
        if (tieneSis) {
            cita.setEstado("CONFIRMADA");
            pago.setMonto(BigDecimal.ZERO);
            pago.setFechaPago(LocalDate.now());
            pago.setMetodoPago("SIS (Seguro Integral)");
            pago.setEstado("PAGADO");
        } else {
            cita.setEstado("PENDIENTE_PAGO");
            pago.setMonto(new BigDecimal("50.00"));
            pago.setFechaPago(null);
            pago.setMetodoPago("Presencial");
            pago.setEstado("PENDIENTE");
        }

        Citas citaGuardada = citasRepository.save(cita);
        pago.setCita(citaGuardada);
        pagoRepository.save(pago);
        return citaGuardada;
    }

    // CANCELAR CITA
    @Transactional
    public void cancelar(Long idCita) {

        Citas cita = obtener(idCita);

        if ("CANCELADA".equalsIgnoreCase(cita.getEstado())) {
            throw new IllegalArgumentException(
                    "La cita ya está cancelada."
            );
        }

        if ("ATENDIDA".equalsIgnoreCase(cita.getEstado())) {
            throw new IllegalArgumentException(
                    "No se puede cancelar una cita atendida."
            );
        }

        cita.setEstado("CANCELADA");

        citasRepository.save(cita);
    }
}
