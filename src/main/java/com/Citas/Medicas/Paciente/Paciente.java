package com.Citas.Medicas.Paciente;

import java.time.LocalDate;

import com.Citas.Medicas.Usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "paciente")
public class Paciente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_paciente")
	private Long idPaciente;

	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

	@Column(name = "numero_historia", length = 20)
	private String numeroHistoria;

	@Column(name = "numero_historia_verificado")
	private Boolean numeroHistoriaVerificado;

	@Column(length = 20)
	private String sis;

	@Column(name = "sis_verificado")
	private Boolean sisVerificado;

	@Column(name = "fecha_nacimiento")
	private LocalDate fechaNacimiento;

	@Column(length = 10)
	private String genero;

	@Column(length = 100)
	private String direccion;

	private Boolean estado;

	public Paciente() {
	}

	// Getters y Setters
	public Long getIdPaciente() {
		return idPaciente;
	}

	public void setIdPaciente(Long idPaciente) {
		this.idPaciente = idPaciente;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getNumeroHistoria() {
		return numeroHistoria;
	}

	public void setNumeroHistoria(String numeroHistoria) {
		this.numeroHistoria = numeroHistoria;
	}

	public Boolean getNumeroHistoriaVerificado() {
		return numeroHistoriaVerificado;
	}

	public void setNumeroHistoriaVerificado(Boolean numeroHistoriaVerificado) {
		this.numeroHistoriaVerificado = numeroHistoriaVerificado;
	}

	public String getSis() {
		return sis;
	}

	public void setSis(String sis) {
		this.sis = sis;
	}

	public Boolean getSisVerificado() {
		return sisVerificado;
	}

	public void setSisVerificado(Boolean sisVerificado) {
		this.sisVerificado = sisVerificado;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public String getGenero() {
		return genero;
	}

	public void setGenero(String genero) {
		this.genero = genero;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}
}
