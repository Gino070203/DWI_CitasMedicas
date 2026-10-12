
package com.Citas.Medicas.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitasRepository
        extends JpaRepository<Citas, Long> {

    // LISTAR TODAS LAS CITAS
    @Query("""
        SELECT c FROM Citas c
        JOIN FETCH c.paciente p
        JOIN FETCH p.usuario
        JOIN FETCH c.medico m
        JOIN FETCH m.usuario
        JOIN FETCH c.horario
        ORDER BY c.fecha DESC, c.horaInicio
    """)
    List<Citas> listarCitas();

    // BUSCAR CITAS POR MÉDICO Y FECHA
    @Query("""
        SELECT c FROM Citas c
        WHERE c.medico.idMedico = :idMedico
          AND c.fecha = :fecha
        ORDER BY c.horaInicio
    """)
    List<Citas> buscarPorMedicoYFecha(
            @Param("idMedico") Long idMedico,
            @Param("fecha") LocalDate fecha
    );

    // BUSCAR CITAS DE UN PACIENTE
    @Query("""
        SELECT c FROM Citas c
        WHERE c.paciente.idPaciente = :idPaciente
        ORDER BY c.fecha DESC
    """)
    List<Citas> buscarPorPaciente(
            @Param("idPaciente") Long idPaciente
    );

    // VERIFICAR CONFLICTOS DE HORARIOS
    @Query("""
        SELECT COUNT(c) FROM Citas c
        WHERE c.medico.idMedico = :idMedico
          AND c.fecha = :fecha
          AND c.estado IN ('CONFIRMADA', 'PENDIENTE_PAGO')
          AND c.horaInicio < :fin
          AND c.horaFin > :inicio
          AND (:excluirId IS NULL OR c.idCita <> :excluirId)
    """)
    long contarConflictos(
            @Param("idMedico") Long idMedico,
            @Param("fecha") LocalDate fecha,
            @Param("inicio") LocalTime inicio,
            @Param("fin") LocalTime fin,
            @Param("excluirId") Long excluirId
    );

    // CONTAR CITAS DE HOY
    @Query("""
        SELECT COUNT(c) FROM Citas c
        WHERE c.fecha = :fecha
    """)
    long contarCitasDelDia(
            @Param("fecha") LocalDate fecha
    );
}
