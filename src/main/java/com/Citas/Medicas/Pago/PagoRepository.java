
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
