package com.Citas.Medicas.Paciente;

public class IdentificadorPacienteDuplicadoException extends IllegalArgumentException {

    private final String campo;

    public IdentificadorPacienteDuplicadoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
