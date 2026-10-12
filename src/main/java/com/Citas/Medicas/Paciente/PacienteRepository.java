package com.Citas.Medicas.Paciente;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PacienteRepository
        extends JpaRepository<Paciente, Long> {


    Optional<Paciente> findByUsuarioIdUsuario(Long idUsuario);

    @Query("""
        SELECT p FROM Paciente p
        JOIN FETCH p.usuario u
        ORDER BY u.apellido, u.nombre
    """)
    List<Paciente> listarPacientes();




    @Query("""
        SELECT p FROM Paciente p
        WHERE p.usuario.idUsuario = :idUsuario
    """)
    Optional<Paciente> buscarPorUsuario(
            @Param("idUsuario") Long idUsuario
    );

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM Paciente p
        WHERE p.usuario.idUsuario = :idUsuario
    """)
    boolean existePorUsuario(
            @Param("idUsuario") Long idUsuario
    );

    @Query("""
        SELECT p FROM Paciente p
        JOIN p.usuario u
        WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR u.dni LIKE CONCAT('%', :texto, '%')
    """)
    List<Paciente> buscarPorNombreODni(
            @Param("texto") String texto
    );

    @Query("""
        SELECT p FROM Paciente p
        WHERE p.estado = true
    """)
    List<Paciente> listarActivos();
}
