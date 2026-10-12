package com.Citas.Medicas.Autenticacion.Servicios;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Paciente.PacienteProvisioningService;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

/** Activa la cuenta y aprovisiona su perfil paciente como una única operación. */
@Service
public class VerificacionRegistroService {

    private final UsuarioRepository usuarios;
    private final PacienteProvisioningService pacientes;

    public VerificacionRegistroService(UsuarioRepository usuarios,
                                      PacienteProvisioningService pacientes) {
        this.usuarios = usuarios;
        this.pacientes = pacientes;
    }

    @Transactional
    public boolean verificarCodigo(String correo, String codigoIngresado) {
        Usuario encontrado = usuarios.findByCorreo(correo).orElse(null);
        if (encontrado == null) {
            return false;
        }

        Usuario usuario = usuarios.bloquearUsuario(encontrado.getIdUsuario())
                .orElse(null);
        if (usuario == null) {
            return false;
        }

        if (Boolean.TRUE.equals(usuario.getEstado())
                && usuario.getCodigoVerificacion() == null) {
            if (usuario.getRol() != null
                    && "PACIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
                pacientes.asegurarPerfilPaciente(usuario.getIdUsuario());
            }
            return true;
        }
        if (usuario.getCodigoVerificacion() == null
                || !usuario.getCodigoVerificacion().equals(codigoIngresado)) {
            return false;
        }

        if (usuario.getRol() != null
                && "PACIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            pacientes.asegurarPerfilPaciente(usuario.getIdUsuario());
        }
        usuario.setEstado(true);
        usuario.setCodigoVerificacion(null);
        usuarios.save(usuario);
        return true;
    }
}
