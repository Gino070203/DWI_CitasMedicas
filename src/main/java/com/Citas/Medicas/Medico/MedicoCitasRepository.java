package com.Citas.Medicas.Medico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Citas.Medicas.Cita.Citas;

public interface MedicoCitasRepository extends JpaRepository<Citas, Long> {

    // Agenda de un día para un médico
    @Query("""
        SELECT c FROM Citas c
        JOIN FETCH c.paciente p
        JOIN FETCH p.usuario
        WHERE c.medico.idMedico = :idMedico
          AND c.fecha = :fecha
        ORDER BY c.horaInicio
    """)
    List<Citas> agendaDelDia(@Param("idMedico") Long idMedico,
                             @Param("fecha") LocalDate fecha);

    // Citas por atender desde una fecha (excluye canceladas, atendidas y no asistió)
    @Query("""
        SELECT c FROM Citas c
        JOIN FETCH c.paciente p
        JOIN FETCH p.usuario
        WHERE c.medico.idMedico = :idMedico
          AND c.fecha >= :desde
          AND (c.estado IS NULL
               OR UPPER(c.estado) NOT IN ('CANCELADA', 'ATENDIDA', 'NO_ASISTIO'))
        ORDER BY c.fecha, c.horaInicio
    """)
    List<Citas> porAtender(@Param("idMedico") Long idMedico,
                           @Param("desde") LocalDate desde);

    // Historial de atenciones del médico
    @Query("""
        SELECT c FROM Citas c
        JOIN FETCH c.paciente p
        JOIN FETCH p.usuario
        WHERE c.medico.idMedico = :idMedico
          AND UPPER(c.estado) = 'ATENDIDA'
        ORDER BY c.fecha DESC, c.horaInicio DESC
    """)
    List<Citas> historial(@Param("idMedico") Long idMedico);

    // Una cita, solo si pertenece a ese médico
    @Query("""
        SELECT c FROM Citas c
        JOIN FETCH c.paciente p
        JOIN FETCH p.usuario
        WHERE c.idCita = :idCita
          AND c.medico.idMedico = :idMedico
    """)
    Optional<Citas> buscarDelMedico(@Param("idCita") Long idCita,
                                    @Param("idMedico") Long idMedico);
}