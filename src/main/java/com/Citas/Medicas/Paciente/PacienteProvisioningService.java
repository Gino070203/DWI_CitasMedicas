package com.Citas.Medicas.Paciente;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

/** Crea de forma segura el perfil paciente que acompaña a una cuenta PACIENTE. */
@Service
public class PacienteProvisioningService {

    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;

    public PacienteProvisioningService(UsuarioRepository usuarios,
                                       PacienteRepository pacientes) {
        this.usuarios = usuarios;
        this.pacientes = pacientes;
    }

    @Transactional
    public Paciente asegurarPerfilPaciente(Long idUsuario) {
        Usuario usuario = usuarios.bloquearUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        validarRolPaciente(usuario);
        return buscarOCrearPerfil(usuario);
    }

    /**
     * Repara también cuentas PACIENTE históricas sin perfil al autenticarse.
     * Devuelve si los datos básicos necesarios para reservar están completos.
     */
    @Transactional
    public boolean asegurarPerfilYComprobarDatos(String correo) {
        Usuario encontrado = usuarios.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Usuario usuario = usuarios.bloquearUsuario(encontrado.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        validarRolPaciente(usuario);
        Paciente paciente = buscarOCrearPerfil(usuario);
        return datosBasicosCompletos(paciente);
    }

    public static boolean datosBasicosCompletos(Paciente paciente) {
        if (paciente == null || paciente.getUsuario() == null) {
            return false;
        }
        Usuario usuario = paciente.getUsuario();
        String genero = paciente.getGenero();
        boolean generoValido = "MASCULINO".equalsIgnoreCase(genero)
                || "FEMENINO".equalsIgnoreCase(genero)
                || "OTRO".equalsIgnoreCase(genero);

        return usuario.getNombre() != null && !usuario.getNombre().isBlank()
                && usuario.getApellido() != null && !usuario.getApellido().isBlank()
                && usuario.getDni() != null && usuario.getDni().matches("[0-9]{8}")
                && paciente.getFechaNacimiento() != null
                && !paciente.getFechaNacimiento().isAfter(java.time.LocalDate.now())
                && generoValido;
    }

    private Paciente buscarOCrearPerfil(Usuario usuario) {
        return pacientes.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseGet(() -> {
                    Paciente paciente = new Paciente();
                    paciente.setUsuario(usuario);
                    paciente.setEstado(true);
                    paciente.setDireccion(usuario.getDireccion());
                    paciente.setNumeroHistoriaVerificado(false);
                    paciente.setSisVerificado(false);
                    // Historia clínica y SIS son datos institucionales:
                    // no se generan valores ficticios durante el alta.
                    return pacientes.save(paciente);
                });
    }

    private void validarRolPaciente(Usuario usuario) {
        if (usuario.getRol() == null
                || !"PACIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            throw new IllegalArgumentException(
                    "Solo los usuarios con rol PACIENTE pueden tener este perfil.");
        }
    }
}
