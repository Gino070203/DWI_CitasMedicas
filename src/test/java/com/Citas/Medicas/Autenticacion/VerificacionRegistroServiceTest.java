package com.Citas.Medicas.Autenticacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Citas.Medicas.Autenticacion.Servicios.VerificacionRegistroService;
import com.Citas.Medicas.Paciente.PacienteProvisioningService;
import com.Citas.Medicas.Roles.Rol;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

class VerificacionRegistroServiceTest {

    private UsuarioRepository usuarios;
    private PacienteProvisioningService provisioning;
    private VerificacionRegistroService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        provisioning = mock(PacienteProvisioningService.class);
        service = new VerificacionRegistroService(usuarios, provisioning);

        Rol rol = new Rol();
        rol.setNombre("PACIENTE");
        usuario = new Usuario();
        usuario.setIdUsuario(81L);
        usuario.setRol(rol);
        usuario.setEstado(false);
        usuario.setCodigoVerificacion("123456");
        when(usuarios.findByCorreo("paciente@example.com"))
                .thenReturn(Optional.of(usuario));
        when(usuarios.bloquearUsuario(81L)).thenReturn(Optional.of(usuario));
    }

    @Test
    void codigoValidoAprovisionaPerfilYActivaLaCuenta() {
        assertTrue(service.verificarCodigo("paciente@example.com", "123456"));

        assertTrue(Boolean.TRUE.equals(usuario.getEstado()));
        assertTrue(usuario.getCodigoVerificacion() == null);
        verify(provisioning).asegurarPerfilPaciente(81L);
        verify(usuarios).save(usuario);
    }

    @Test
    void codigoInvalidoNoActivaCuentaNiCreaPerfil() {
        assertFalse(service.verificarCodigo("paciente@example.com", "000000"));

        assertFalse(Boolean.TRUE.equals(usuario.getEstado()));
        verify(provisioning, never()).asegurarPerfilPaciente(81L);
        verify(usuarios, never()).save(usuario);
    }

    @Test
    void siFallaElAprovisionamientoLaCuentaNoSeActiva() {
        when(provisioning.asegurarPerfilPaciente(81L))
                .thenThrow(new IllegalStateException("No se pudo guardar el perfil"));

        try {
            service.verificarCodigo("paciente@example.com", "123456");
        } catch (IllegalStateException expected) {
            // Se verifica que la activación ocurre después del aprovisionamiento.
        }

        assertFalse(Boolean.TRUE.equals(usuario.getEstado()));
        assertTrue("123456".equals(usuario.getCodigoVerificacion()));
        verify(usuarios, never()).save(usuario);
    }
}
