package com.Citas.Medicas.Medico;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Especialidad.Especialidad;
import com.Citas.Medicas.Especialidad.EspecialidadRepository;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

@Service
public class MedicoService {

    private final MedicoRepository medicos;
    private final UsuarioRepository usuarios;
    private final EspecialidadRepository especialidades;

    public MedicoService(MedicoRepository medicos,
                         UsuarioRepository usuarios,
                         EspecialidadRepository especialidades) {
        this.medicos = medicos;
        this.usuarios = usuarios;
        this.especialidades = especialidades;
    }

    @Transactional(readOnly = true)
    public List<Medico> listar() {
        return medicos.listarMedicos();
    }

    @Transactional(readOnly = true)
    public List<Medico> filtrar(String texto, Long idEspecialidad, Boolean estado) {
        String criterio = texto == null || texto.isBlank() ? null : texto.trim();
        return medicos.filtrarMedicos(criterio, idEspecialidad, estado);
    }

    @Transactional(readOnly = true)
    public Medico obtener(Long id) {
        return medicos.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
    }

    @Transactional
    public Medico registrar(Long idUsuario, Long idEspecialidad, String cmp) {
        Usuario usuario = usuarios.bloquearUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getRol() == null || !"MEDICO".equals(usuario.getRol().getNombre()))
            throw new IllegalArgumentException("El usuario no tiene rol MEDICO");
        if (medicos.existePorUsuario(idUsuario))
            throw new IllegalArgumentException("Ese usuario ya tiene perfil de médico");

        String cmpLimpio = cmp == null ? "" : cmp.trim();
        if (cmpLimpio.isEmpty())
            throw new IllegalArgumentException("El CMP es obligatorio");
        if (medicos.existePorCmp(cmpLimpio))
            throw new IllegalArgumentException("El CMP ya está registrado");

        Especialidad esp = obtenerEspecialidadActiva(idEspecialidad);

        Medico medico = new Medico();
        medico.setUsuario(usuario);
        medico.setCmp(cmpLimpio);
        medico.setEspecialidad(esp);
        medico.setEstado(true);
        return medicos.save(medico);
    }

    @Transactional
    public Medico actualizar(Long idMedico, Long idEspecialidad, String cmp) {
        Medico medico = obtener(idMedico);

        String cmpLimpio = cmp == null ? "" : cmp.trim();
        if (cmpLimpio.isEmpty())
            throw new IllegalArgumentException("El CMP es obligatorio");
        if (medicos.existePorCmpExcluyendo(cmpLimpio, idMedico))
            throw new IllegalArgumentException("El CMP ya pertenece a otro médico");

        medico.setCmp(cmpLimpio);
        medico.setEspecialidad(obtenerEspecialidadActiva(idEspecialidad));
        return medico;
    }

    // Alterna activo/inactivo (desactivación lógica)
    @Transactional
    public void cambiarEstado(Long idMedico) {
        Medico medico = obtener(idMedico);
        medico.setEstado(!Boolean.TRUE.equals(medico.getEstado()));
    }

    private Especialidad obtenerEspecialidadActiva(Long id) {
        Especialidad esp = especialidades.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
        if (!Boolean.TRUE.equals(esp.getEstado()))
            throw new IllegalArgumentException("La especialidad está inactiva");
        return esp;
    }
}
