package com.Citas.Medicas.Autenticacion.Admin;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Cita.CitasRepository;
import com.Citas.Medicas.Especialidad.EspecialidadRepository;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Paciente.PacienteRepository;
import com.Citas.Medicas.Pago.PagoRepository;
import com.Citas.Medicas.Usuario.UsuarioRepository;

@Service
public class DashboardService {

    private final UsuarioRepository usuarios;
    private final MedicoRepository medicos;
    private final PacienteRepository pacientes;
    private final EspecialidadRepository especialidades;
    private final CitasRepository citas;
    private final PagoRepository pagos;

    public DashboardService(
            UsuarioRepository usuarios,
            MedicoRepository medicos,
            PacienteRepository pacientes,
            EspecialidadRepository especialidades,
            CitasRepository citas,
            PagoRepository pagos) {
        this.usuarios = usuarios;
        this.medicos = medicos;
        this.pacientes = pacientes;
        this.especialidades = especialidades;
        this.citas = citas;
        this.pagos = pagos;
    }

    @Transactional(readOnly = true)
    public long totalUsuarios() {
        return usuarios.count();
    }

    @Transactional(readOnly = true)
    public long totalMedicos() {
        return medicos.count();
    }

    @Transactional(readOnly = true)
    public long totalPacientes() {
        return pacientes.count();
    }

    @Transactional(readOnly = true)
    public long totalEspecialidades() {
        return especialidades.count();
    }

    @Transactional(readOnly = true)
    public long totalCitas() {
        return citas.count();
    }

    @Transactional(readOnly = true)
    public long citasHoy() {
        return citas.contarCitasDelDia(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long totalPagos() {
        return pagos.count();
    }
}