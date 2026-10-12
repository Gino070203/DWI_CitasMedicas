package com.Citas.Medicas.Paciente;

public class DniPacienteDuplicadoException extends RuntimeException {

    public DniPacienteDuplicadoException() {
        super("Ese DNI ya está asociado a otra cuenta.");
    }
}
