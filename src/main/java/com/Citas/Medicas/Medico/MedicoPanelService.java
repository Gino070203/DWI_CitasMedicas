package com.Citas.Medicas.Medico;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Cita.Citas;

@Service
public class MedicoPanelService {

    private static final Set<String> CERRADAS = Set.of("CANCELADA", "ATENDIDA", "NO_ASISTIO");

    private final MedicoRepository medicos;
    private final MedicoCitasRepository citas;

    public MedicoPanelService(MedicoRepository medicos, MedicoCitasRepository citas) {
        this.medicos = medicos;
        this.citas = citas;
    }

    @Transactional(readOnly = true)
    public Medico medicoActual(String correo) {
        return medicos.buscarPorCorreo(correo)
                .orElseThrow(() -> new IllegalStateException(
                        "Tu usuario no tiene perfil de médico registrado"));
    }

    @Transactional(readOnly = true)
    public List<Citas> agendaDeHoy(String correo) {
        Medico m = medicoActual(correo);
        return citas.agendaDelDia(m.getIdMedico(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Citas> porAtender(String correo) {
        Medico m = medicoActual(correo);
        return citas.porAtender(m.getIdMedico(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Citas> historial(String correo) {
        Medico m = medicoActual(correo);
        return citas.historial(m.getIdMedico());
    }

    @Transactional(readOnly = true)
    public Citas obtenerCita(String correo, Long idCita) {
        Medico m = medicoActual(correo);
        return citas.buscarDelMedico(idCita, m.getIdMedico())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
    }

    // RN07: solo su propia cita y solo si todavía admite atención
    @Transactional
    public void registrarDiagnostico(String correo, Long idCita, String diagnostico) {
        Citas cita = obtenerCita(correo, idCita);

        String estado = cita.getEstado() == null ? "" : cita.getEstado().toUpperCase();
        if (CERRADAS.contains(estado))
            throw new IllegalArgumentException("Esta cita ya no admite diagnóstico (" + estado + ")");

        String texto = diagnostico == null ? "" : diagnostico.trim();
        if (texto.isEmpty())
            throw new IllegalArgumentException("El diagnóstico es obligatorio");
        if (texto.length() > 500)
            throw new IllegalArgumentException("Máximo 500 caracteres");

        cita.setDiagnostico(texto);
        cita.setEstado("ATENDIDA");   // JPA guarda al cerrar la transacción
    }
}