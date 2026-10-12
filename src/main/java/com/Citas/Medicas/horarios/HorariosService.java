package com.Citas.Medicas.Horarios;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Medico.MedicoRepository;

@Service
public class HorariosService {

    private final HorariosRepository horarios;
    private final MedicoRepository medicos;

    public HorariosService(HorariosRepository horarios,
                           MedicoRepository medicos) {
        this.horarios = horarios;
        this.medicos = medicos;
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