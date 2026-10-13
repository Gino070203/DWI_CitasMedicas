package com.Citas.Medicas.Paciente;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PacienteRepository
        extends JpaRepository<Paciente, Long> {


    Optional<Paciente> findByUsuarioIdUsuario(Long idUsuario);

    boolean existsByNumeroHistoriaIgnoreCase(String numeroHistoria);

    boolean existsByNumeroHistoriaIgnoreCaseAndIdPacienteNot(
            String numeroHistoria, Long idPaciente);

    boolean existsBySisIgnoreCase(String sis);

    boolean existsBySisIgnoreCaseAndIdPacienteNot(String sis, Long idPaciente);

    @Query("""
        SELECT p FROM Paciente p
        JOIN FETCH p.usuario u
        ORDER BY u.apellido, u.nombre
    """)
    List<Paciente> listarPacientes();

    @Query("""
        SELECT p FROM Paciente p
        JOIN FETCH p.usuario u
        WHERE (:texto IS NULL
               OR LOWER(COALESCE(u.nombre, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(u.apellido, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(u.dni, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(u.correo, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(p.numeroHistoria, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(p.sis, '')) LIKE CONCAT('%', LOWER(:texto), '%'))
          AND (:estado IS NULL OR p.estado = :estado)
          AND (:conSis IS NULL
               OR (:conSis = true AND p.sis IS NOT NULL AND TRIM(p.sis) <> '')
               OR (:conSis = false AND (p.sis IS NULL OR TRIM(p.sis) = '')))
        ORDER BY u.apellido, u.nombre
    """)
    List<Paciente> filtrarPacientes(
            @Param("texto") String texto,
            @Param("estado") Boolean estado,
            @Param("conSis") Boolean conSis);




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
