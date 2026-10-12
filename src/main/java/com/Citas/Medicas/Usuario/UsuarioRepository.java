package com.Citas.Medicas.Usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    // Necesario para tu login actual.
    Optional<Usuario> findByCorreo(String correo);

    @Query("""
        SELECT u FROM Usuario u
        ORDER BY u.apellido, u.nombre
    """)
    List<Usuario> listarUsuarios();

    @Query("""
        SELECT u FROM Usuario u
        WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR u.dni LIKE CONCAT('%', :texto, '%')
    """)
    List<Usuario> buscar(
            @Param("texto") String texto
    );

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
        FROM Usuario u
        WHERE LOWER(u.correo) = LOWER(:correo)
          AND (:id IS NULL OR u.idUsuario <> :id)
    """)
    boolean existeCorreoDeOtro(
            @Param("correo") String correo,
            @Param("id") Long id
    );

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
        FROM Usuario u
        WHERE u.dni = :dni
          AND (:id IS NULL OR u.idUsuario <> :id)
    """)
    boolean existeDniDeOtro(
            @Param("dni") String dni,
            @Param("id") Long id
    );

    @Query("""
        SELECT u FROM Usuario u
        WHERE u.rol.nombre = :rol
        ORDER BY u.apellido, u.nombre
    """)
    List<Usuario> buscarPorRol(
            @Param("rol") String rol
    );

	@org.springframework.data.jpa.repository.Lock(
    jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
)
@Query("""
    SELECT u FROM Usuario u
    WHERE u.idUsuario = :id
""")
Optional<Usuario> bloquearUsuario(
        @Param("id") Long id
);


}