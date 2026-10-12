package com.Citas.Medicas.Paciente;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

@Service
public class PerfilPacienteService {

    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;

    public PerfilPacienteService(UsuarioRepository usuarios,
                                 PacienteRepository pacientes) {
        this.usuarios = usuarios;
        this.pacientes = pacientes;
    }

    @Transactional(readOnly = true)
    public PerfilPacienteVista obtenerPerfil(Long idUsuario) {
        Usuario usuario = obtenerUsuarioPaciente(idUsuario);
        Paciente paciente = obtenerPacientePropio(idUsuario);

        PerfilPacienteForm formulario = new PerfilPacienteForm();
        formulario.setNombre(usuario.getNombre());
        formulario.setApellido(usuario.getApellido());
        formulario.setDni(usuario.getDni());
        formulario.setTelefono(usuario.getTelefono());
        formulario.setDireccion(usuario.getDireccion() != null
                && !usuario.getDireccion().isBlank()
                ? usuario.getDireccion() : paciente.getDireccion());
        formulario.setFechaNacimiento(paciente.getFechaNacimiento());
        formulario.setGenero(paciente.getGenero());

        return new PerfilPacienteVista(
                formulario,
                usuario.getCorreo(),
                paciente.getNumeroHistoria(),
                paciente.getSis());
    }

    @Transactional
    public void actualizarPerfil(Long idUsuario, PerfilPacienteForm formulario) {
        if (formulario == null) {
            throw new IllegalArgumentException("No se recibieron datos del perfil.");
        }

        Usuario usuario = usuarios.bloquearUsuario(idUsuario)
                .orElseThrow(() -> new IllegalStateException("No se encontró la cuenta."));
        asegurarRolPaciente(usuario);
        Paciente paciente = obtenerPacientePropio(idUsuario);

        String dni = normalizar(formulario.getDni());
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos.");
        }
        if (usuarios.existeDniDeOtro(dni, idUsuario)) {
            throw new DniPacienteDuplicadoException();
        }

        LocalDate nacimiento = formulario.getFechaNacimiento();
        if (nacimiento == null || nacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }

        String nombre = normalizar(formulario.getNombre());
        String apellido = normalizar(formulario.getApellido());
        String genero = normalizar(formulario.getGenero());
        if (nombre == null || apellido == null) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios.");
        }
        if (!"MASCULINO".equalsIgnoreCase(genero)
                && !"FEMENINO".equalsIgnoreCase(genero)
                && !"OTRO".equalsIgnoreCase(genero)) {
            throw new IllegalArgumentException("Selecciona una opción válida de género.");
        }

        String telefono = normalizar(formulario.getTelefono());
        String direccion = normalizar(formulario.getDireccion());
        if (telefono != null && telefono.length() > 20) {
            throw new IllegalArgumentException("El teléfono no puede superar los 20 caracteres.");
        }
        if (direccion != null && direccion.length() > 100) {
            throw new IllegalArgumentException("La dirección no puede superar los 100 caracteres.");
        }

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setDni(dni);
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);

        paciente.setFechaNacimiento(nacimiento);
        paciente.setGenero(genero.toUpperCase(Locale.ROOT));
        // La dirección existe en ambas entidades; mantenerlas sincronizadas.
        paciente.setDireccion(direccion);

        usuarios.save(usuario);
        pacientes.save(paciente);
    }

    private Usuario obtenerUsuarioPaciente(Long idUsuario) {
        Usuario usuario = usuarios.findById(idUsuario)
                .orElseThrow(() -> new IllegalStateException("No se encontró la cuenta."));
        asegurarRolPaciente(usuario);
        return usuario;
    }

    private Paciente obtenerPacientePropio(Long idUsuario) {
        return pacientes.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalStateException(
                        "No encontramos un perfil de paciente asociado a tu cuenta."));
    }

    private void asegurarRolPaciente(Usuario usuario) {
        if (usuario.getRol() == null
                || !"PACIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            throw new IllegalStateException("Esta sección solo está disponible para pacientes.");
        }
    }

    private static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim().replaceAll("\\s+", " ");
    }
}
