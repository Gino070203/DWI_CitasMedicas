package com.Citas.Medicas.Usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Paciente.PacienteProvisioningService;
import com.Citas.Medicas.Paciente.PacienteRepository;
import com.Citas.Medicas.Roles.Rol;
import com.Citas.Medicas.Roles.RolRepository;

class UsuarioAdminServiceTest {

    private UsuarioRepository usuarios;
    private RolRepository roles;
    private PacienteProvisioningService provisioning;
    private UsuarioAdminService service;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        roles = mock(RolRepository.class);
        provisioning = mock(PacienteProvisioningService.class);
        service = new UsuarioAdminService(
                usuarios,
                roles,
                mock(MedicoRepository.class),
                mock(PacienteRepository.class),
                provisioning,
                mock(PasswordEncoder.class));

        Rol rol = new Rol();
        rol.setNombre("PACIENTE");
        when(roles.findByNombre("PACIENTE")).thenReturn(java.util.Optional.of(rol));
        when(usuarios.existeCorreoDeOtro("ana@example.com", null)).thenReturn(false);
        when(usuarios.existeDniDeOtro("12345678", null)).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            usuario.setIdUsuario(48L);
            return usuario;
        });
    }

    @Test
    void creaElPerfilCuandoAdministracionAsignaRolPaciente() {
        Usuario usuario = service.guardar(null, "Ana", "Pérez", "12345678",
                "ana@example.com", null, null, "Password123", "PACIENTE");

        assertEquals(48L, usuario.getIdUsuario());
        verify(provisioning).asegurarPerfilPaciente(48L);
    }
}
