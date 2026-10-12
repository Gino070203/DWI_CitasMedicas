package com.Citas.Medicas.Pago;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.Citas.Medicas.Cita.Citas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pago")
public class Pago {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_pago")
	private Long idPago;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cita", nullable = false)
	private Citas cita;

	@Column(precision = 10, scale = 2)
	private BigDecimal monto;

	@Column(name = "fecha_pago")
	private LocalDate fechaPago;

	@Column(name = "metodo_pago", length = 50)
	private String metodoPago;

	@Column(length = 20)
	private String estado;

	public Pago() {
	}

	// Getters y Setters
	public Long getIdPago() {
		return idPago;
	}

	public void setIdPago(Long idPago) {
		this.idPago = idPago;
	}

	public Citas getCita() {
		return cita;
	}

	public void setCita(Citas cita) {
		this.cita = cita;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public LocalDate getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago(LocalDate fechaPago) {
		this.fechaPago = fechaPago;
	}

	public String getMetodoPago() {
		return metodoPago;
	}

	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
}