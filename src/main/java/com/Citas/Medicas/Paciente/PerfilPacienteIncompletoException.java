package com.Citas.Medicas.Paciente;

public class PerfilPacienteIncompletoException extends IllegalArgumentException {

    public PerfilPacienteIncompletoException() {
        super("Completa tu nombre, DNI, fecha de nacimiento y género antes de reservar.");
    }
}
