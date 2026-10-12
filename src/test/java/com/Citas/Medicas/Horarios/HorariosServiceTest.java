package com.Citas.Medicas.Horarios;

import com.Citas.Medicas.horarios.Horarios;
import com.Citas.Medicas.horarios.HorariosRepository;
import com.Citas.Medicas.horarios.HorariosService;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Citas.Medicas.Medico.Medico;
import com.Citas.Medicas.Medico.MedicoRepository;

class HorariosServiceTest {

    private HorariosRepository horariosRepository;
    private MedicoRepository medicoRepository;
    private HorariosService service;

    @BeforeEach
    void setUp() {
        horariosRepository = mock(HorariosRepository.class);
        medicoRepository = mock(MedicoRepository.class);
        service = new HorariosService(horariosRepository, medicoRepository, 30);
    }

    @Test
    void rechazaUnBloqueMasCortoQueLaDuracionDeConsulta() {
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(null, 4L, "MONDAY",
                        LocalTime.of(9, 0), LocalTime.of(9, 29)));

        verify(medicoRepository, never()).bloquearMedico(any());
        verify(horariosRepository, never()).save(any(Horarios.class));
    }

    @Test
    void aceptaUnBloqueExactamenteDeLaDuracionConfigurada() {
        Medico medico = new Medico();
        medico.setIdMedico(4L);
        medico.setEstado(true);
        when(medicoRepository.bloquearMedico(4L)).thenReturn(Optional.of(medico));
        when(horariosRepository.contarConflictos(
                4L, "MONDAY", LocalTime.of(9, 0), LocalTime.of(9, 30), null))
                .thenReturn(0L);
        when(horariosRepository.save(any(Horarios.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        service.guardar(null, 4L, "MONDAY",
                LocalTime.of(9, 0), LocalTime.of(9, 30));

        verify(horariosRepository).save(any(Horarios.class));
    }
}
