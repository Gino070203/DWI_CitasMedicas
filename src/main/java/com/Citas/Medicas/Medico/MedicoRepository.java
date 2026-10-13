package com.Citas.Medicas.Medico;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    @Query("""
        SELECT m FROM Medico m JOIN FETCH m.usuario u JOIN FETCH m.especialidad
        ORDER BY u.apellido, u.nombre
    """)
    List<Medico> listarMedicos();

    @Query("""
        SELECT m FROM Medico m JOIN FETCH m.usuario u JOIN FETCH m.especialidad
        WHERE m.estado = true
        ORDER BY u.apellido, u.nombre
    """)
    List<Medico> listarMedicosActivos();

    @Query("""
        SELECT m FROM Medico m JOIN FETCH m.usuario JOIN FETCH m.especialidad
        WHERE m.idMedico = :id
    """)
    Optional<Medico> buscarPorId(@Param("id") Long id);

    @Query("""
        SELECT m FROM Medico m JOIN FETCH m.usuario u JOIN FETCH m.especialidad e
        WHERE e.idEspecialidad = :idEspecialidad AND m.estado = true
    """)
    List<Medico> listarActivosPorEspecialidad(@Param("idEspecialidad") Long idEspecialidad);

    @Query("SELECT COUNT(m) > 0 FROM Medico m WHERE m.usuario.idUsuario = :idUsuario")
    boolean existePorUsuario(@Param("idUsuario") Long idUsuario);

    @Query("SELECT COUNT(m) > 0 FROM Medico m WHERE m.cmp = :cmp")
    boolean existePorCmp(@Param("cmp") String cmp);

    @Query("SELECT COUNT(m) > 0 FROM Medico m WHERE m.cmp = :cmp AND m.idMedico <> :id")
    boolean existePorCmpExcluyendo(@Param("cmp") String cmp, @Param("id") Long id);

    // Bloqueo para evitar choques de horarios/citas concurrentes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Medico m WHERE m.idMedico = :id")
    Optional<Medico> bloquearMedico(@Param("id") Long id);

        @Query("""
        SELECT m FROM Medico m JOIN FETCH m.usuario u JOIN FETCH m.especialidad
        WHERE LOWER(u.correo) = LOWER(:correo)
    """)
    Optional<Medico> buscarPorCorreo(@Param("correo") String correo);
}