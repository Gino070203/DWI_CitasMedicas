package com.Citas.Medicas.Paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

class PerfilPacienteServiceTest {

    private UsuarioRepository usuarios;
    private PacienteRepository pacientes;
    private PerfilPacienteService service;
    private Usuario usuario;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        pacientes = mock(PacienteRepository.class);
        service = new PerfilPacienteService(usuarios, pacientes);

        Rol rolPaciente = new Rol();
        rolPaciente.setNombre("PACIENTE");
        usuario = new Usuario();
        usuario.setIdUsuario(23L);
        usuario.setRol(rolPaciente);
        usuario.setNombre("Ana");
        usuario.setApellido("Pérez");
        usuario.setCorreo("ana@example.com");
        usuario.setContraseña("hash-anterior");
        usuario.setEstado(true);

        paciente = new Paciente();
        paciente.setIdPaciente(41L);
        paciente.setUsuario(usuario);
        paciente.setEstado(true);
        paciente.setNumeroHistoria("HC-1042");
        paciente.setSis("SIS-12345");

        when(usuarios.bloquearUsuario(23L)).thenReturn(Optional.of(usuario));
        when(pacientes.findByUsuarioIdUsuario(23L)).thenReturn(Optional.of(paciente));
        when(usuarios.existeDniDeOtro("12345678", 23L)).thenReturn(false);
    }

    @Test
    void actualizaLosDatosPermitidosYSincronizaLaDireccion() {
        PerfilPacienteForm formulario = formularioValido();

        service.actualizarPerfil(23L, formulario);

        assertEquals("Ana María", usuario.getNombre());
        assertEquals("Pérez-Ríos", usuario.getApellido());
        assertEquals("12345678", usuario.getDni());
        assertEquals("+51 987 654 321", usuario.getTelefono());
        assertEquals("Av. Salud 123", usuario.getDireccion());
        assertEquals("Av. Salud 123", paciente.getDireccion());
        assertEquals(LocalDate.of(1995, 4, 15), paciente.getFechaNacimiento());
        assertEquals("FEMENINO", paciente.getGenero());
        assertEquals("ana@example.com", usuario.getCorreo());
        assertEquals("hash-anterior", usuario.getContraseña());
        assertEquals("HC-1042", paciente.getNumeroHistoria());
        assertEquals("SIS-12345", paciente.getSis());
        verify(usuarios).save(usuario);
        verify(pacientes).save(paciente);
    }

    @Test
    void rechazaUnDniQuePerteneceAOtroUsuarioSinGuardarCambios() {
        when(usuarios.existeDniDeOtro("12345678", 23L)).thenReturn(true);

        assertThrows(DniPacienteDuplicadoException.class,
                () -> service.actualizarPerfil(23L, formularioValido()));

        verify(usuarios, never()).save(usuario);
        verify(pacientes, never()).save(paciente);
    }

    @Test
    void cargaDatosEditablesYProtegidosDesdeLaCuentaPropia() {
        when(usuarios.findById(23L)).thenReturn(Optional.of(usuario));

        PerfilPacienteVista vista = service.obtenerPerfil(23L);

        assertEquals("Ana", vista.formulario().getNombre());
        assertEquals("ana@example.com", vista.correo());
        assertEquals("HC-1042", vista.numeroHistoria());
        assertEquals("SIS-12345", vista.sis());
    }

    private static PerfilPacienteForm formularioValido() {
        PerfilPacienteForm formulario = new PerfilPacienteForm();
        formulario.setNombre("  Ana María ");
        formulario.setApellido("Pérez-Ríos");
        formulario.setDni("12345678");
        formulario.setTelefono("+51 987 654 321");
        formulario.setDireccion(" Av. Salud 123 ");
        formulario.setFechaNacimiento(LocalDate.of(1995, 4, 15));
        formulario.setGenero("FEMENINO");
        return formulario;
    }
}
