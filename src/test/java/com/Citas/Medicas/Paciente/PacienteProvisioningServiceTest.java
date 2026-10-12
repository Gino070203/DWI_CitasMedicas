package com.Citas.Medicas.Paciente;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Citas.Medicas.Roles.Rol;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

class PacienteProvisioningServiceTest {

    private UsuarioRepository usuarios;
    private PacienteRepository pacientes;
    private PacienteProvisioningService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        pacientes = mock(PacienteRepository.class);
        service = new PacienteProvisioningService(usuarios, pacientes);

        Rol rol = new Rol();
        rol.setNombre("PACIENTE");
        usuario = new Usuario();
        usuario.setIdUsuario(71L);
        usuario.setRol(rol);
        usuario.setNombre("Ana");
        usuario.setApellido("Pérez");
        usuario.setDni("12345678");
        usuario.setDireccion("Av. Salud 123");

        when(usuarios.bloquearUsuario(71L)).thenReturn(Optional.of(usuario));
    }

    @Test
    void creaPerfilMinimoEnlazadoSinInventarHistoriaNiSis() {
        when(pacientes.findByUsuarioIdUsuario(71L)).thenReturn(Optional.empty());
        when(pacientes.save(org.mockito.ArgumentMatchers.any(Paciente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Paciente paciente = service.asegurarPerfilPaciente(71L);

        assertSame(usuario, paciente.getUsuario());
        assertTrue(Boolean.TRUE.equals(paciente.getEstado()));
        assertTrue(paciente.getNumeroHistoria() == null);
        assertTrue(paciente.getSis() == null);
        assertTrue(paciente.getFechaNacimiento() == null);
        assertTrue(paciente.getGenero() == null);
        assertTrue("Av. Salud 123".equals(paciente.getDireccion()));
        verify(usuarios).bloquearUsuario(71L);
        verify(pacientes).save(paciente);
    }

    @Test
    void reutilizaElPerfilExistenteSinCrearUnDuplicado() {
        Paciente existente = new Paciente();
        existente.setUsuario(usuario);
        when(pacientes.findByUsuarioIdUsuario(71L)).thenReturn(Optional.of(existente));

        Paciente obtenido = service.asegurarPerfilPaciente(71L);

        assertSame(existente, obtenido);
        verify(pacientes, never()).save(org.mockito.ArgumentMatchers.any(Paciente.class));
    }

    @Test
    void reparaUnaCuentaPacienteAntiguaAlAutenticarse() {
        when(usuarios.findByCorreo("ana@example.com")).thenReturn(Optional.of(usuario));
        when(pacientes.findByUsuarioIdUsuario(71L)).thenReturn(Optional.empty());
        when(pacientes.save(org.mockito.ArgumentMatchers.any(Paciente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        boolean perfilCompleto = service.asegurarPerfilYComprobarDatos("ana@example.com");

        assertFalse(perfilCompleto);
        verify(usuarios).bloquearUsuario(71L);
        verify(pacientes).save(org.mockito.ArgumentMatchers.any(Paciente.class));
    }

    @Test
    void detectaSiLosDatosNecesariosParaReservarEstanCompletos() {
        Paciente paciente = new Paciente();
        paciente.setUsuario(usuario);
        assertFalse(PacienteProvisioningService.datosBasicosCompletos(paciente));

        paciente.setFechaNacimiento(LocalDate.of(1990, 2, 3));
        paciente.setGenero("FEMENINO");
        assertTrue(PacienteProvisioningService.datosBasicosCompletos(paciente));
    }
}
