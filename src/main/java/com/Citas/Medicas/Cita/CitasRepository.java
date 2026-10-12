package com.Citas.Medicas.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CitasRepository extends JpaRepository<Citas, Long> {

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
          AND (c.estado IS NULL OR UPPER(c.estado) <> 'CANCELADA')
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

    // Fechas ocupadas para los médicos visibles en un rango acotado.
    @Query("""
        SELECT c FROM Citas c
        WHERE c.medico.idMedico IN :idsMedico
          AND c.fecha BETWEEN :desde AND :hasta
          AND (c.estado IS NULL OR UPPER(c.estado) <> 'CANCELADA')
        ORDER BY c.fecha, c.horaInicio
    """)
    List<Citas> buscarNoCanceladasPorMedicosEnRango(
            @Param("idsMedico") List<Long> idsMedico,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    // CONTAR CITAS DE HOY
    @Query("""
        SELECT COUNT(c) FROM Citas c
        WHERE c.fecha = :fecha
    """)
    long contarCitasDelDia(
            @Param("fecha") LocalDate fecha
    );

    // JPQL para obtener todas las citas de un paciente específico usando su idPaciente
    @Query("SELECT c FROM Citas c WHERE c.paciente.idPaciente = :idPaciente ORDER BY c.fecha DESC, c.horaInicio DESC")
    List<Citas> findByPacienteId(@Param("idPaciente") Long idPaciente);

    // JPQL para buscar citas pendientes o confirmadas de un paciente
    @Query("SELECT c FROM Citas c WHERE c.paciente.idPaciente = :idPaciente AND c.estado = :estado")
    List<Citas> findByPacienteIdAndEstado(@Param("idPaciente") Long idPaciente, @Param("estado") String estado);

}
