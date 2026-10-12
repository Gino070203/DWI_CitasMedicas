package com.Citas.Medicas.Medico;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface MedicoRepository
        extends JpaRepository<Medico, Long> {

    @Query("""
        SELECT m FROM Medico m
        JOIN FETCH m.usuario u
        JOIN FETCH m.especialidad e
        ORDER BY u.apellido, u.nombre
    """)
    List<Medico> listarMedicos();

    @Query("""
        SELECT m FROM Medico m
        WHERE m.usuario.idUsuario = :idUsuario
    """)
    Optional<Medico> buscarPorUsuario(
            @Param("idUsuario") Long idUsuario
    );

    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM Medico m
        WHERE m.usuario.idUsuario = :idUsuario
    """)
    boolean existePorUsuario(
            @Param("idUsuario") Long idUsuario
    );

    @Query("""
        SELECT m FROM Medico m
        WHERE LOWER(m.cmp) = LOWER(:cmp)
    """)
    Optional<Medico> buscarPorCmp(
            @Param("cmp") String cmp
    );

    @Query("""
        SELECT m FROM Medico m
        WHERE m.estado = true
        ORDER BY m.usuario.apellido
    """)
    List<Medico> listarMedicosActivos();

    @Query("""
        SELECT m FROM Medico m
        WHERE m.especialidad.idEspecialidad = :id
          AND m.estado = true
    """)
    List<Medico> buscarPorEspecialidad(
            @Param("id") Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT m FROM Medico m
        WHERE m.idMedico = :id
    """)
    Optional<Medico> bloquearMedico(
            @Param("id") Long id
    );
}