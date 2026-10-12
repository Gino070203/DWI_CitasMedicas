package com.Citas.Medicas.Paciente;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.Citas.Medicas.Cita.Citas;
import com.Citas.Medicas.Cita.CitasService;
import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Roles.Rol;
import com.Citas.Medicas.Usuario.UsuarioRepository;

class PacienteServiceReservaTest {

    @Test
    void delegaLaReservaConElTurnoSeleccionadoAlServicioComun() {
        CitasService citasService = mock(CitasService.class);
        PacienteService pacienteService = new PacienteService(
                mock(PacienteRepository.class),
                mock(UsuarioRepository.class),
                mock(MedicoRepository.class));
        ReflectionTestUtils.setField(pacienteService, "citasService", citasService);

        LocalDate fecha = LocalDate.of(2026, 10, 19);
        LocalTime inicio = LocalTime.of(9, 30);
        Citas cita = new Citas();
        when(citasService.reservarPaciente(5L, 12L, fecha, inicio)).thenReturn(cita);

        Citas resultado = pacienteService.reservarCita(5L, 12L, fecha, inicio);

        assertSame(cita, resultado);
        verify(citasService).reservarPaciente(5L, 12L, fecha, inicio);
    }

    @Test
    void administracionCompletaUnPerfilEsqueletoSinCrearOtroPaciente() {
        PacienteRepository pacientes = mock(PacienteRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        MedicoRepository medicos = mock(MedicoRepository.class);
        PacienteService pacienteService = new PacienteService(
                pacientes, usuarios, medicos);

        Rol rol = new Rol();
        rol.setNombre("PACIENTE");
        com.Citas.Medicas.Usuario.Usuario usuario =
                new com.Citas.Medicas.Usuario.Usuario();
        usuario.setIdUsuario(5L);
        usuario.setRol(rol);
        when(usuarios.bloquearUsuario(5L)).thenReturn(Optional.of(usuario));
        when(medicos.existePorUsuario(5L)).thenReturn(false);

        Paciente esqueleto = new Paciente();
        esqueleto.setUsuario(usuario);
        esqueleto.setEstado(true);
        when(pacientes.findByUsuarioIdUsuario(5L)).thenReturn(Optional.of(esqueleto));
        when(pacientes.save(any(Paciente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Paciente completado = pacienteService.registrar(5L, "HC-55", null,
                LocalDate.of(1990, 2, 3), "FEMENINO", "Av. Salud 4");

        assertSame(esqueleto, completado);
        assertEquals("HC-55", completado.getNumeroHistoria());
        assertEquals(LocalDate.of(1990, 2, 3), completado.getFechaNacimiento());
        verify(pacientes).save(esqueleto);
    }
}
