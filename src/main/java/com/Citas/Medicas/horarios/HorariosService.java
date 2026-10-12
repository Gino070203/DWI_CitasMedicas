package com.Citas.Medicas.horarios;

import java.time.LocalTime;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Medico.MedicoRepository;

@Service
public class HorariosService {

    private final HorariosRepository horarios;
    private final MedicoRepository medicos;
    private final int duracionConsultaMinutos;

    public HorariosService(HorariosRepository horarios,
                           MedicoRepository medicos,
                           @Value("${agenda.duracion-consulta-minutos:30}") int duracionConsultaMinutos) {
        this.horarios = horarios;
        this.medicos = medicos;
        if (duracionConsultaMinutos <= 0) {
            throw new IllegalArgumentException(
                    "La duración de consulta debe ser mayor que cero.");
        }
        this.duracionConsultaMinutos = duracionConsultaMinutos;
    }

    public int getDuracionConsultaMinutos() {
        return duracionConsultaMinutos;
    }

    public int contarTurnosPosibles(Horarios horario) {
        if (horario == null || horario.getHoraInicio() == null
                || horario.getHoraFin() == null
                || !horario.getHoraInicio().isBefore(horario.getHoraFin())) {
            return 0;
        }
        long minutos = Duration.between(
                horario.getHoraInicio(), horario.getHoraFin()).toMinutes();
        return (int) (minutos / duracionConsultaMinutos);
    }

    @Transactional(readOnly = true)
    public List<Horarios> listar() {
        return horarios.listarHorarios();
    }

    @Transactional(readOnly = true)
    public Horarios obtener(Long id) {
        return horarios.findById(id).orElseThrow(() ->
            new IllegalArgumentException("Horario no encontrado"));
    }

    @Transactional
    public Horarios guardar(Long id, Long idMedico,
                            String dia, LocalTime inicio,
                            LocalTime fin) {

        if (dia == null || !List.of(
                "MONDAY", "TUESDAY", "WEDNESDAY",
                "THURSDAY", "FRIDAY", "SATURDAY",
                "SUNDAY").contains(dia)) {
            throw new IllegalArgumentException("Día inválido");
        }

        if (inicio == null || fin == null ||
                !inicio.isBefore(fin)) {
            throw new IllegalArgumentException("Horas inválidas");
        }

        long duracionBloque = Duration.between(inicio, fin).toMinutes();
        if (duracionBloque < duracionConsultaMinutos) {
            throw new IllegalArgumentException(
                    "El bloque debe durar al menos "
                            + duracionConsultaMinutos + " minutos para admitir una cita.");
        }

        Medico medico = medicos.bloquearMedico(idMedico)
            .orElseThrow(() ->
                new IllegalArgumentException("Médico no encontrado"));

        if (!Boolean.TRUE.equals(medico.getEstado())) {
            throw new IllegalArgumentException("Médico inactivo");
        }

        if (horarios.contarConflictos(
                idMedico, dia, inicio, fin, id) > 0) {
            throw new IllegalArgumentException(
                "El horario se superpone con otro");
        }

        Horarios horario = id == null
            ? new Horarios() : obtener(id);

        // No cambiar horarios existentes con citas futuras
        // sin comprobar primero dichas citas.
        horario.setMedico(medico);
        horario.setDiaSemana(dia);
        horario.setHoraInicio(inicio);
        horario.setHoraFin(fin);

        if (id == null) {
            horario.setEstado(true);
        }

        return horarios.save(horario);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Horarios h = obtener(id);
        h.setEstado(!Boolean.TRUE.equals(h.getEstado()));
        horarios.save(h);
    }
}
