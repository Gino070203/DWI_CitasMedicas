package com.Citas.Medicas.Cita;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Citas.Medicas.Horarios.HorariosService;

@SpringBootTest(properties = "agenda.duracion-consulta-minutos=17")
class DuracionCitasConfigTest {

    @Autowired
    private CitasService citasService;

    @Autowired
    private HorariosService horariosService;

    @Test
    void usaLaMismaDuracionConfiguradaEnAmbosServicios() {
        assertEquals(17, citasService.getDuracionConsultaMinutos());
        assertEquals(17, horariosService.getDuracionConsultaMinutos());
    }
}
