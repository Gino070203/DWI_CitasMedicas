
package com.Citas.Medicas.Pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Cita.Citas;
import com.Citas.Medicas.Cita.CitasRepository;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CitasRepository citasRepository;

    public PagoService(
            PagoRepository pagoRepository,
            CitasRepository citasRepository) {

        this.pagoRepository = pagoRepository;
        this.citasRepository = citasRepository;
    }

    @Transactional(readOnly = true)
    public List<Pago> listar() {
        return pagoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pago> filtrar(String texto, String estado, String metodo,
                              LocalDate desde, LocalDate hasta) {
        String criterio = texto == null || texto.isBlank() ? null : texto.trim();
        String estadoLimpio = estado == null || estado.isBlank() ? null : estado.trim();
        String metodoLimpio = metodo == null || metodo.isBlank() ? null : metodo.trim();
        Long idRegistro = null;
        if (criterio != null && criterio.matches("[0-9]+")) {
            try {
                idRegistro = Long.valueOf(criterio);
            } catch (NumberFormatException ignored) {
                // Se conserva la búsqueda textual si el número excede Long.
            }
        }
        return pagoRepository.filtrarPagos(
                criterio, idRegistro, estadoLimpio, metodoLimpio, desde, hasta);
    }


    @Transactional(readOnly = true)
    public Pago obtener(Long idPago) {

        return pagoRepository.findById(idPago)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pago solicitado no existe."
                        ));
    }
    @Transactional
    public Pago registrar(
            Long idCita,
            BigDecimal monto,
            String metodoPago) {

        // 3.1 VALIDAR DATOS
        if (idCita == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una cita médica."
            );
        }

        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El monto del pago debe ser mayor que cero."
            );
        }

        if (metodoPago == null ||
                metodoPago.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debes seleccionar un método de pago."
            );
        }

        // 3.2 VALIDAR MÉTODO DE PAGO
        String metodo = metodoPago.trim().toUpperCase();

        if (!metodo.equals("EFECTIVO") &&
                !metodo.equals("TARJETA") &&
                !metodo.equals("TRANSFERENCIA")) {

            throw new IllegalArgumentException(
                    "El método de pago seleccionado no es válido."
            );
        }

        // 3.3 BUSCAR LA CITA
        Citas cita = citasRepository.findById(idCita)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La cita médica no existe."
                        ));

        if (cita.getPaciente() == null) {
            throw new IllegalArgumentException(
                    "La cita no tiene un paciente asignado."
            );
        }

        String numeroSis = cita.getPaciente().getSis();

        boolean tieneSis = numeroSis != null
                && !numeroSis.trim().isEmpty()
                && !Boolean.FALSE.equals(cita.getPaciente().getSisVerificado());

        if (tieneSis) {

            throw new IllegalArgumentException(
                    "El paciente tiene SIS registrado. " +
                    "No corresponde realizar un pago particular."
            );
        }

        if (!"PENDIENTE_PAGO".equalsIgnoreCase(
                cita.getEstado())) {

            throw new IllegalArgumentException(
                    "La cita no está pendiente de pago. " +
                    "Estado actual: " + cita.getEstado()
            );
        }
        boolean pagoExistente = pagoRepository.findAll()
                .stream()
                .anyMatch(p ->
                        p.getCita() != null &&
                        idCita.equals(
                                p.getCita().getIdCita()
                        ) &&
                        "PAGADO".equalsIgnoreCase(
                                p.getEstado()
                        )
                );

        if (pagoExistente) {
            throw new IllegalArgumentException(
                    "Esta cita ya tiene un pago registrado."
            );
        }
        Pago pago = new Pago();

        pago.setCita(cita);
        pago.setMonto(monto);
        pago.setFechaPago(LocalDate.now());
        pago.setMetodoPago(metodo);
        pago.setEstado("PAGADO");

        Pago pagoGuardado = pagoRepository.save(pago);

        cita.setEstado("CONFIRMADA");

        citasRepository.save(cita);

        return pagoGuardado;
    }

    @Transactional(readOnly = true)
    public BigDecimal totalRecaudado() {

        return pagoRepository.findAll()
                .stream()
                .filter(p ->
                        "PAGADO".equalsIgnoreCase(
                                p.getEstado()
                        )
                )
                .map(Pago::getMonto)
                .filter(monto -> monto != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}
