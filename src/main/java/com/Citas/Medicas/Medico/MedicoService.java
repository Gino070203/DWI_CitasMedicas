package com.Citas.Medicas.Medico;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Especialidad.Especialidad;
import com.Citas.Medicas.Especialidad.EspecialidadRepository;
import com.Citas.Medicas.Paciente.PacienteRepository;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

@Service
public class MedicoService {

    private final MedicoRepository medicos;
    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;
    private final EspecialidadRepository especialidades;

    public MedicoService(
            MedicoRepository medicos,
            UsuarioRepository usuarios,
            PacienteRepository pacientes,
            EspecialidadRepository especialidades) {
        this.medicos = medicos;
        this.usuarios = usuarios;
        this.pacientes = pacientes;
        this.especialidades = especialidades;
    }

    @Transactional(readOnly = true)
    public List<Medico> listar() {
        return medicos.listarMedicos();
    }

    @Transactional(readOnly = true)
    public Medico obtener(Long id) {
        return medicos.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Médico no encontrado"));
    }

    @Transactional
    public Medico registrar(Long idUsuario,
                            Long idEspecialidad,
                            String cmp) {

        Usuario usuario = usuarios.bloquearUsuario(idUsuario)
            .orElseThrow(() ->
                new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getRol() == null ||
            !"MEDICO".equalsIgnoreCase(
                usuario.getRol().getNombre())) {
            throw new IllegalArgumentException(
                "El usuario no tiene rol MEDICO");
        }

        if (medicos.existePorUsuario(idUsuario)
                || pacientes.existePorUsuario(idUsuario)) {
            throw new IllegalArgumentException(
                "El usuario ya tiene un perfil");
        }

        Especialidad especialidad = obtenerEspecialidad(idEspecialidad);
        validarCmp(cmp, null);

        Medico medico = new Medico();
        medico.setUsuario(usuario);
        medico.setEspecialidad(especialidad);
        medico.setCmp(cmp.trim());
        medico.setEstado(true);

        return medicos.save(medico);
    }

    @Transactional
    public Medico actualizar(Long id, Long idEspecialidad,
                             String cmp) {
        Medico medico = obtener(id);

        validarCmp(cmp, id);

        medico.setCmp(cmp.trim());
        medico.setEspecialidad(obtenerEspecialidad(idEspecialidad));

        return medicos.save(medico);
    }

    private Especialidad obtenerEspecialidad(Long id) {
        Especialidad e = especialidades.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Especialidad no encontrada"));

        if (!Boolean.TRUE.equals(e.getEstado())) {
            throw new IllegalArgumentException(
                "La especialidad está inactiva");
        }
        return e;
    }

    private void validarCmp(String cmp, Long idActual) {
        if (cmp == null || cmp.isBlank() || cmp.trim().length() > 20) {
            throw new IllegalArgumentException("CMP inválido");
        }

        medicos.buscarPorCmp(cmp.trim()).ifPresent(m -> {
            if (idActual == null || !m.getIdMedico().equals(idActual)) {
                throw new IllegalArgumentException(
                    "El CMP ya está registrado");
            }
        });
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Medico medico = obtener(id);
        medico.setEstado(!Boolean.TRUE.equals(medico.getEstado()));
        medicos.save(medico);
    }
}