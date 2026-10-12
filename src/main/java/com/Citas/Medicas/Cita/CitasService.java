
package com.Citas.Medicas.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Horarios.Horarios;
import com.Citas.Medicas.Horarios.HorariosRepository;
import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Paciente.Paciente;
import com.Citas.Medicas.Paciente.PacienteRepository;

@Service
public class CitasService {

    private final CitasRepository citasRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final HorariosRepository horariosRepository;

    public CitasService(
            CitasRepository citasRepository,
            PacienteRepository pacienteRepository,
            MedicoRepository medicoRepository,
            HorariosRepository horariosRepository) {

        this.citasRepository = citasRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.horariosRepository = horariosRepository;
    }

    // LISTAR TODAS LAS CITAS
    @Transactional(readOnly = true)
    public List<Citas> listar() {
        return citasRepository.findAll();
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

    // REGISTRAR NUEVA CITA
    @Transactional
    public Citas registrar(
            Long idPaciente,
            Long idMedico,
            Long idHorario,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        // 1. OBTENER PACIENTE
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El paciente no existe."
                        ));

        // 2. OBTENER MÉDICO
        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El médico no existe."
                        ));

        // 3. OBTENER HORARIO
        Horarios horario = horariosRepository.findById(idHorario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El horario no existe."
                        ));

        // 4. VALIDAR FECHA
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de la cita no puede ser anterior a hoy."
            );
        }

        // 5. VALIDAR HORAS
        if (horaInicio == null || horaFin == null
                || !horaInicio.isBefore(horaFin)) {

            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin."
            );
        }

        // 6. VALIDAR ESTADOS
        if (!Boolean.TRUE.equals(paciente.getEstado())) {
            throw new IllegalArgumentException(
                    "El paciente está inactivo."
            );
        }

        if (!Boolean.TRUE.equals(medico.getEstado())) {
            throw new IllegalArgumentException(
                    "El médico está inactivo."
            );
        }

        if (!Boolean.TRUE.equals(horario.getEstado())) {
            throw new IllegalArgumentException(
                    "El horario está inactivo."
            );
        }

        // 7. VALIDAR QUE EL HORARIO PERTENEZCA AL MÉDICO
        if (!horario.getMedico().getIdMedico().equals(idMedico)) {
            throw new IllegalArgumentException(
                    "El horario seleccionado no pertenece al médico."
            );
        }

        // 8. VALIDAR DÍA DE ATENCIÓN
        String diaFecha = fecha.getDayOfWeek().name();

        if (!diaFecha.equalsIgnoreCase(horario.getDiaSemana())) {
            throw new IllegalArgumentException(
                    "La fecha no corresponde al día de atención del médico."
            );
        }

        // 9. VALIDAR RANGO DE HORAS
        if (horaInicio.isBefore(horario.getHoraInicio())
                || horaFin.isAfter(horario.getHoraFin())) {

            throw new IllegalArgumentException(
                    "La cita debe estar dentro del horario del médico."
            );
        }

        // 10. VERIFICAR CRUCE DE CITAS
        List<Citas> citasDelDia =
                citasRepository.findAll().stream()
                        .filter(c -> c.getMedico() != null)
                        .filter(c -> idMedico.equals(
                                c.getMedico().getIdMedico()))
                        .filter(c -> fecha.equals(c.getFecha()))
                        .filter(c -> !"CANCELADA".equalsIgnoreCase(
                                c.getEstado()))
                        .toList();

        for (Citas existente : citasDelDia) {

            boolean hayCruce =
                    horaInicio.isBefore(existente.getHoraFin())
                    && horaFin.isAfter(existente.getHoraInicio());

            if (hayCruce) {
                throw new IllegalArgumentException(
                        "El médico ya tiene una cita en ese horario."
                );
            }
        }

        // 11. CREAR CITA
        Citas cita = new Citas();

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setHorario(horario);
        cita.setFecha(fecha);
        cita.setHoraInicio(horaInicio);
        cita.setHoraFin(horaFin);

        // 12. VALIDAR SIS
        String numeroSis = paciente.getSis();

        boolean tieneSis =
                numeroSis != null
                && !numeroSis.trim().isEmpty();

        // 13. ASIGNAR ESTADO AUTOMÁTICAMENTE
        if (tieneSis) {

            // PACIENTE CON SIS
            // NO NECESITA PAGO PARTICULAR
            cita.setEstado("CONFIRMADA");

        } else {

            // PACIENTE SIN SIS
            // DEBE REGISTRAR SU PAGO
            cita.setEstado("PENDIENTE_PAGO");
        }

        // 14. GUARDAR CITA
        return citasRepository.save(cita);
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
