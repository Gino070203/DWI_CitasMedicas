package com.Citas.Medicas.Usuario;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Roles.Rol;
import com.Citas.Medicas.Roles.RolRepository;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Paciente.PacienteRepository;

@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final MedicoRepository medicos;
    private final PacienteRepository pacientes;
    private final PasswordEncoder encoder;

    public UsuarioAdminService(
            UsuarioRepository usuarios,
            RolRepository roles,
            MedicoRepository medicos,
            PacienteRepository pacientes,
            PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.medicos = medicos;
        this.pacientes = pacientes;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarios.listarUsuarios();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarios.findById(id).orElseThrow(() ->
            new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional
    public Usuario guardar(Long id, String nombre,
                           String apellido, String dni,
                           String correo, String telefono,
                           String direccion, String password,
                           String nombreRol) {

        if (nombre == null || nombre.isBlank()
                || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException(
                "Nombre y apellido obligatorios");
        }

        if (correo == null || !correo.matches(
                "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Correo inválido");
        }

        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                "El DNI debe tener 8 dígitos");
        }

        if (nombreRol == null ||
            !List.of("ADMIN", "MEDICO", "PACIENTE")
                .contains(nombreRol)) {
            throw new IllegalArgumentException("Rol inválido");
        }

        String correoLimpio =
            correo.trim().toLowerCase(Locale.ROOT);

        if (usuarios.existeCorreoDeOtro(correoLimpio, id)) {
            throw new IllegalArgumentException(
                "El correo ya está registrado");
        }

        if (usuarios.existeDniDeOtro(dni, id)) {
            throw new IllegalArgumentException(
                "El DNI ya está registrado");
        }

        Rol rol = roles.findByNombre(nombreRol)
            .orElseThrow(() ->
                new IllegalArgumentException("Rol no encontrado"));

        Usuario usuario = id == null
            ? new Usuario() : obtener(id);

        if (id != null && usuario.getRol() != null &&
                !nombreRol.equals(usuario.getRol().getNombre()) &&
                (medicos.existePorUsuario(id) ||
                 pacientes.existePorUsuario(id))) {
            throw new IllegalArgumentException(
                "No se puede cambiar el rol de un perfil existente");
        }

        usuario.setNombre(nombre.trim());
        usuario.setApellido(apellido.trim());
        usuario.setDni(dni);
        usuario.setCorreo(correoLimpio);
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);
        usuario.setRol(rol);

        if (id == null) {
            if (password == null || password.length() < 8) {
                throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres");
            }

            usuario.setContraseña(encoder.encode(password));
            usuario.setEstado(true);
        } else if (password != null && !password.isBlank()) {
            if (password.length() < 8) {
                throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres");
            }
            usuario.setContraseña(encoder.encode(password));
        }

        return usuarios.save(usuario);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Usuario u = obtener(id);

        if ("ADMIN".equals(u.getRol().getNombre()) &&
                Boolean.TRUE.equals(u.getEstado())) {
            throw new IllegalArgumentException(
                "La desactivación de administradores requiere " +
                "comprobar que exista otro administrador activo");
        }

        u.setEstado(!Boolean.TRUE.equals(u.getEstado()));
        usuarios.save(u);
    }
}