package com.Citas.Medicas.Paciente;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

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
        formulario.setNumeroHistoria(paciente.getNumeroHistoria());
        formulario.setSis(paciente.getSis());
        formulario.setFechaNacimiento(paciente.getFechaNacimiento());
        formulario.setGenero(paciente.getGenero());

        return new PerfilPacienteVista(
                formulario,
                usuario.getCorreo(),
                paciente.getNumeroHistoria(),
                paciente.getSis(),
                tieneValor(paciente.getNumeroHistoria())
                        && !Boolean.FALSE.equals(paciente.getNumeroHistoriaVerificado()),
                tieneValor(paciente.getSis())
                        && !Boolean.FALSE.equals(paciente.getSisVerificado()));
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

        String historiaActual = normalizar(paciente.getNumeroHistoria());
        String sisActual = normalizar(paciente.getSis());
        String historiaSolicitada = normalizar(formulario.getNumeroHistoria());
        String sisSolicitado = normalizar(formulario.getSis());

        // Un campo vacío no borra un identificador ya registrado; para una
        // corrección se debe ingresar el nuevo valor explícitamente.
        String numeroHistoria = historiaSolicitada != null
                ? historiaSolicitada : historiaActual;
        String sis = sisSolicitado != null ? sisSolicitado : sisActual;
        validarLongitudIdentificadores(numeroHistoria, sis);

        boolean cambioHistoria = !Objects.equals(historiaActual, numeroHistoria);
        boolean cambioSis = !Objects.equals(sisActual, sis);
        if (cambioHistoria && numeroHistoria != null
                && pacientes.existsByNumeroHistoriaIgnoreCaseAndIdPacienteNot(
                        numeroHistoria, paciente.getIdPaciente())) {
            throw new IdentificadorPacienteDuplicadoException(
                    "numeroHistoria", "Ese número de historia ya pertenece a otro paciente.");
        }
        if (cambioSis && sis != null
                && pacientes.existsBySisIgnoreCaseAndIdPacienteNot(
                        sis, paciente.getIdPaciente())) {
            throw new IdentificadorPacienteDuplicadoException(
                    "sis", "Ese número SIS ya pertenece a otro paciente.");
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
        if (cambioHistoria) {
            paciente.setNumeroHistoria(numeroHistoria);
            paciente.setNumeroHistoriaVerificado(false);
        }
        if (cambioSis) {
            paciente.setSis(sis);
            paciente.setSisVerificado(false);
        }
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

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static void validarLongitudIdentificadores(String historia, String sis) {
        if (historia != null && historia.length() > 20) {
            throw new IllegalArgumentException(
                    "El número de historia no puede superar 20 caracteres.");
        }
        if (sis != null && sis.length() > 20) {
            throw new IllegalArgumentException("El SIS no puede superar 20 caracteres.");
        }
    }
}
