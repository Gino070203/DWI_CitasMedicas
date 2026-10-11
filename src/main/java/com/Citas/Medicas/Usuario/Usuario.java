package com.Citas.Medicas.Usuario;

import com.Citas.Medicas.Roles.Rol;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Usuario {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idUsuario;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rol_id",nullable = false)
	private Rol rol;
	
	private String dni;
	private String correo;
	private String contraseña;
	private String nombre;
	private String apellido;
	private String telefono;
	private String direccion;
	private Boolean estado;
	private String codigoVerificacion;
	
	public Usuario() {
	}
	
	public Usuario(Rol rol, String dni, String correo, String contraseña, String nombre, String apellido,
			String telefono, String direccion, Boolean estado) {
		this.rol = rol;
		this.dni = dni;
		this.correo = correo;
		this.contraseña = contraseña;
		this.nombre = nombre;
		this.apellido = apellido;
		this.telefono = telefono;
		this.direccion = direccion;
		this.estado = estado;
	}

	

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getContraseña() {
		return contraseña;
	}

	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
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
	
	public String getCodigoVerificacion() {
	    return codigoVerificacion;
	}

	public void setCodigoVerificacion(String codigoVerificacion) {
	    this.codigoVerificacion = codigoVerificacion;
	}

}
