package com.Citas.Medicas.Paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Citas.Medicas.Pago.Pago;

public interface PacienteRepository extends JpaRepository<Paciente, Long>{

}
