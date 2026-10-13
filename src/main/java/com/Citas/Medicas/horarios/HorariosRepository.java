package com.Citas.Medicas.horarios;
import java.time.LocalTime;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface HorariosRepository
        extends JpaRepository<Horarios, Long> {

	// JPQL para buscar horarios activos de un médico según la especialidad

    @Query("SELECT h FROM Horarios h WHERE h.medico.especialidad.idEspecialidad = :idEspecialidad AND h.estado = true AND h.medico.estado = true")
    List<Horarios> findByEspecialidadId(@Param("idEspecialidad") Long idEspecialidad);

    // JPQL para buscar horarios por especialidad y un día de semana específico

    @Query("SELECT h FROM Horarios h WHERE h.medico.especialidad.idEspecialidad = :idEspecialidad AND h.diaSemana = :diaSemana AND h.estado = true")
    List<Horarios> findByEspecialidadAndDia(@Param("idEspecialidad") Long idEspecialidad, @Param("diaSemana") String diaSemana);


    @Query("""
        SELECT h FROM Horarios h
        JOIN FETCH h.medico m
        JOIN FETCH m.usuario
        ORDER BY h.diaSemana, h.horaInicio
    """)
    List<Horarios> listarHorarios();

    @Query("""
        SELECT h FROM Horarios h
        JOIN FETCH h.medico m
        JOIN FETCH m.usuario u
        JOIN FETCH m.especialidad e
        WHERE (:texto IS NULL
               OR LOWER(COALESCE(u.nombre, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(u.apellido, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(u.dni, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(m.cmp, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(e.nombre, '')) LIKE CONCAT('%', LOWER(:texto), '%'))
          AND (:diaSemana IS NULL OR UPPER(h.diaSemana) = UPPER(:diaSemana))
          AND (:estado IS NULL OR h.estado = :estado)
        ORDER BY u.apellido, u.nombre, h.diaSemana, h.horaInicio
    """)
    List<Horarios> filtrarHorarios(
            @Param("texto") String texto,
            @Param("diaSemana") String diaSemana,
            @Param("estado") Boolean estado);

    @Query("""
        SELECT h FROM Horarios h
        JOIN FETCH h.medico m
        JOIN FETCH m.usuario
        WHERE h.estado = true AND m.estado = true
        ORDER BY h.diaSemana, h.horaInicio
    """)
    List<Horarios> listarHorariosActivos();

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
