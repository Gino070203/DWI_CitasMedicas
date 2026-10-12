package com.Citas.Medicas.Especialidad;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EspecialidadRepository
        extends JpaRepository<Especialidad, Long> {

    @Query("""
        SELECT e FROM Especialidad e
        ORDER BY e.nombre
    """)
    List<Especialidad> listarTodas();

    @Query("""
        SELECT e FROM Especialidad e
        WHERE e.estado = true
        ORDER BY e.nombre
    """)
    List<Especialidad> listarActivas();

    @Query("""
        SELECT e FROM Especialidad e
        WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
    """)
    List<Especialidad> buscarPorNombre(
            @Param("texto") String texto
    );

    @Query("""
        SELECT e FROM Especialidad e
        WHERE LOWER(e.nombre) = LOWER(:nombre)
    """)
    Optional<Especialidad> buscarNombreExacto(
            @Param("nombre") String nombre
    );
}