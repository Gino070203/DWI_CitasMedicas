package com.Citas.Medicas.Cita;

import java.time.LocalDate;
import java.time.LocalTime;

public class Cita {
    private Long idCita;
    private Long idPaciente;
    private Long idMedico;
    private Long idHorario;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String diagnostico;
    private String estado;

    // Constructor vacío
    public Cita() {
    }

    // Constructor con parámetros
    public Cita(Long idCita, Long idPaciente, Long idMedico, Long idHorario, 
                LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, 
                String diagnostico, String estado) {
        this.idCita = idCita;
        this.idPaciente = idPaciente;
        this.idMedico = idMedico;
        this.idHorario = idHorario;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.diagnostico = diagnostico;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getIdCita() { return idCita; }
    public void setIdCita(Long idCita) { this.idCita = idCita; }

    public Long getIdPaciente() { return idPaciente; }
    public void setIdPaciente(Long idPaciente) { this.idPaciente = idPaciente; }

    public Long getIdMedico() { return idMedico; }
    public void setIdMedico(Long idMedico) { this.idMedico = idMedico; }

    public Long getIdHorario() { return idHorario; }
    public void setIdHorario(Long idHorario) { this.idHorario = idHorario; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}