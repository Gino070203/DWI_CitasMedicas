package com.Citas.Medicas.Paciente;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

import com.Citas.Medicas.Cita.Citas;
import com.Citas.Medicas.Cita.CitasService;
import com.Citas.Medicas.Cita.CitasRepository;
import com.Citas.Medicas.horarios.Horarios;
import com.Citas.Medicas.horarios.HorariosRepository;

@Service
public class PacienteService {

	@Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private HorariosRepository horarioRepository;

    @Autowired
    private CitasRepository citaRepository;

    @Autowired
    private CitasService citasService;

    public Paciente obtenerPacientePorUsuarioId(Long idUsuario) {
        return pacienteRepository.findByUsuarioIdUsuario(idUsuario).orElse(null);
    }

    // Listar horarios disponibles por especialidad
    public List<Horarios> obtenerHorariosPorEspecialidad(Long idEspecialidad) {
        return horarioRepository.findByEspecialidadId(idEspecialidad).stream()
                .filter(horario -> !citasService
                        .generarTurnosDentroDelBloque(horario).isEmpty())
                .toList();
    }

    public List<Citas> obtenerHistorialCitas(Long idPaciente) {
        return citaRepository.findByPacienteId(idPaciente);
    }

    /** Delega la reserva para que use el mismo cálculo que administración. */
    @Transactional
    public Citas reservarCita(Long idPaciente, Long idHorario,
                              LocalDate fechaCita, LocalTime horaInicio) {
        return citasService.reservarPaciente(
                idPaciente, idHorario, fechaCita, horaInicio);
    }


    private final PacienteRepository pacientes;
    private final UsuarioRepository usuarios;
    private final MedicoRepository medicos;

    public PacienteService(PacienteRepository pacientes,
                           UsuarioRepository usuarios,
                           MedicoRepository medicos) {
        this.pacientes = pacientes;
        this.usuarios = usuarios;
        this.medicos = medicos;
    }

    @Transactional(readOnly = true)
    public List<Paciente> listar() {
        return pacientes.listarPacientes();
    }

    @Transactional(readOnly = true)
    public Paciente obtener(Long id) {
        return pacientes.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Paciente no encontrado"));
    }

    @Transactional
    public Paciente registrar(Long idUsuario, String historia,
                              String sis, LocalDate nacimiento,
                              String genero, String direccion) {

        Usuario usuario = usuarios.bloquearUsuario(idUsuario)
            .orElseThrow(() ->
                new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getRol() == null ||
            !"PACIENTE".equalsIgnoreCase(
                usuario.getRol().getNombre())) {
            throw new IllegalArgumentException(
                "El usuario no tiene rol PACIENTE");
        }

        if (medicos.existePorUsuario(idUsuario)) {
            throw new IllegalArgumentException(
                "El usuario ya tiene un perfil");
        }

        Paciente pacienteExistente = pacientes.findByUsuarioIdUsuario(idUsuario)
                .orElse(null);
        if (pacienteExistente != null
                && pacienteExistente.getNumeroHistoria() != null
                && !pacienteExistente.getNumeroHistoria().isBlank()) {
            throw new IllegalArgumentException(
                    "El usuario ya tiene un perfil de paciente completo; utiliza la opción de edición.");
        }

        Paciente paciente = pacienteExistente != null
                ? pacienteExistente : new Paciente();
        paciente.setUsuario(usuario);
        paciente.setEstado(true);

        asignarDatos(paciente, historia, sis, nacimiento,
                     genero, direccion);

        return pacientes.save(paciente);
    }

    @Transactional
    public Paciente actualizar(Long id, String historia,
                               String sis, LocalDate nacimiento,
                               String genero, String direccion) {
        Paciente paciente = obtener(id);

        asignarDatos(paciente, historia, sis, nacimiento,
                     genero, direccion);

        return pacientes.save(paciente);
    }

    private void asignarDatos(Paciente p, String historia,
                              String sis, LocalDate nacimiento,
                              String genero, String direccion) {

        if (historia == null || historia.isBlank()
                || historia.trim().length() > 20) {
            throw new IllegalArgumentException(
                "Número de historia inválido");
        }

        if (nacimiento == null || nacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Fecha de nacimiento inválida");
        }

        if (sis != null && sis.length() > 20) {
            throw new IllegalArgumentException("SIS demasiado largo");
        }

        if (genero == null || genero.length() > 10) {
            throw new IllegalArgumentException("Género inválido");
        }

        if (direccion != null && direccion.length() > 100) {
            throw new IllegalArgumentException("Dirección demasiado larga");
        }

        p.setNumeroHistoria(historia.trim());
        p.setSis(sis);
        p.setFechaNacimiento(nacimiento);
        p.setGenero(genero);
        p.setDireccion(direccion);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Paciente paciente = obtener(id);
        paciente.setEstado(!Boolean.TRUE.equals(paciente.getEstado()));
        pacientes.save(paciente);
    }
}
