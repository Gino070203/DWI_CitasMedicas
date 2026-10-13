
package com.Citas.Medicas.Pago;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    // LISTAR TODOS LOS PAGOS
    @Query("""
        SELECT p FROM Pago p
        JOIN FETCH p.cita c
        JOIN FETCH c.paciente
        JOIN FETCH c.medico
        ORDER BY p.fechaPago DESC
    """)
    List<Pago> listarPagos();

    @Query("""
        SELECT DISTINCT p FROM Pago p
        LEFT JOIN FETCH p.cita c
        LEFT JOIN FETCH c.paciente pa
        LEFT JOIN FETCH pa.usuario u
        LEFT JOIN FETCH c.medico m
        LEFT JOIN FETCH m.usuario mu
        WHERE (:texto IS NULL
               OR LOWER(COALESCE(u.nombre, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(u.apellido, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(u.dni, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR LOWER(COALESCE(p.metodoPago, '')) LIKE CONCAT('%', LOWER(:texto), '%')
               OR (:idRegistro IS NOT NULL AND
                   (p.idPago = :idRegistro OR c.idCita = :idRegistro)))
          AND (:estado IS NULL OR UPPER(p.estado) = UPPER(:estado))
          AND (:metodo IS NULL OR UPPER(p.metodoPago) = UPPER(:metodo))
          AND (:desde IS NULL OR p.fechaPago >= :desde)
          AND (:hasta IS NULL OR p.fechaPago <= :hasta)
        ORDER BY p.fechaPago DESC, p.idPago DESC
    """)
    List<Pago> filtrarPagos(
            @Param("texto") String texto,
            @Param("idRegistro") Long idRegistro,
            @Param("estado") String estado,
            @Param("metodo") String metodo,
            @Param("desde") java.time.LocalDate desde,
            @Param("hasta") java.time.LocalDate hasta);

    // BUSCAR PAGOS DE UNA CITA
    @Query("""
        SELECT p FROM Pago p
        WHERE p.cita.idCita = :idCita
    """)
    List<Pago> buscarPorCita(
            @Param("idCita") Long idCita
    );

    // VERIFICAR SI EXISTE UN PAGO CONFIRMADO
    @Query("""
        SELECT CASE WHEN COUNT(p) > 0
            THEN true ELSE false END
        FROM Pago p
        WHERE p.cita.idCita = :idCita
          AND p.estado = 'PAGADO'
    """)
    boolean existePagoConfirmado(
            @Param("idCita") Long idCita
    );

    // CALCULAR TOTAL RECAUDADO
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Pago p
        WHERE p.estado = 'PAGADO'
    """)
    BigDecimal totalRecaudado();
}
