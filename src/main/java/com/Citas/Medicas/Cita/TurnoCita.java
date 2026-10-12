package com.Citas.Medicas.Cita;

import java.time.LocalTime;

/** Intervalo reservable calculado dentro de un bloque semanal. */
public record TurnoCita(LocalTime horaInicio, LocalTime horaFin) {
}
