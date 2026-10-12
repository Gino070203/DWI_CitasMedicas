package com.Citas.Medicas.Horarios;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorariosRepository
        extends JpaRepository<Horarios, Long> {

    @Query("""
        SELECT h FROM Horarios h
        JOIN FETCH h.medico m
        JOIN FETCH m.usuario
        ORDER BY h.diaSemana, h.horaInicio
    """)
    List<Horarios> listarHorarios();

    @Query("""
        SELECT h FROM Horarios h
        WHERE h.medico.idMedico = :idMedico
        ORDER BY h.diaSemana, h.horaInicio
    """)
    List<Horarios> buscarPorMedico(
            @Param("idMedico") Long idMedico
    );

    @Query("""
        SELECT COUNT(h) FROM Horarios h
        WHERE h.medico.idMedico = :idMedico
          AND h.diaSemana = :dia
          AND h.estado = true
          AND h.horaInicio < :fin
          AND h.horaFin > :inicio
          AND (:excluirId IS NULL OR h.idHorario <> :excluirId)
    """)
    long contarConflictos(
            @Param("idMedico") Long idMedico,
            @Param("dia") String dia,
            @Param("inicio") LocalTime inicio,
            @Param("fin") LocalTime fin,
            @Param("excluirId") Long excluirId
    );
}