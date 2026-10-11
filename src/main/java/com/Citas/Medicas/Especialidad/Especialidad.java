package com.Citas.Medicas.Especialidad;

import jakarta.persistence.*;

@Entity
@Table(name = "especialidad")
public class Especialidad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_especialidad")
	private Long idEspecialidad;

	@Column(length = 100, nullable = false)
	private String nombre;

	@Column(length = 200)
	private String descripcion;

	private Boolean estado;

	public Especialidad() {
	}

	// Getters y Setters
	public Long getIdEspecialidad() {
		return idEspecialidad;
	}

	public void setIdEspecialidad(Long idEspecialidad) {
		this.idEspecialidad = idEspecialidad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}
}
