package com.Citas.Medicas.horarios;

import jakarta.persistence.*;
import java.time.LocalTime;

import com.Citas.Medicas.Medico.Medico;

@Entity
@Table(name = "horarios")
public class Horarios {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_horario")
	private Long idHorario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_medico", nullable = false)
	private Medico medico;

	@Column(name = "dia_semana", length = 15)
	private String diaSemana;

	@Column(name = "horainicio")
	private LocalTime horaInicio;

	@Column(name = "horafin")
	private LocalTime horaFin;

	private Boolean estado;

	public Horarios() {
	}

	// Getters y Setters
	public Long getIdHorario() {
		return idHorario;
	}

	public void setIdHorario(Long idHorario) {
		this.idHorario = idHorario;
	}

	public Medico getMedico() {
		return medico;
	}

	public void setMedico(Medico medico) {
		this.medico = medico;
	}

	public String getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(String diaSemana) {
		this.diaSemana = diaSemana;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(LocalTime horaFin) {
		this.horaFin = horaFin;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}
}
