package com.Citas.Medicas.Paciente;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PerfilPacienteForm {

    @NotBlank(message = "Ingresa tu nombre.")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres.")
    @Pattern(regexp = "^[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*$",
            message = "Usa letras, espacios, apóstrofos o guiones en el nombre.")
    private String nombre;

    @NotBlank(message = "Ingresa tu apellido.")
    @Size(max = 60, message = "El apellido no puede superar los 60 caracteres.")
    @Pattern(regexp = "^[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*$",
            message = "Usa letras, espacios, apóstrofos o guiones en el apellido.")
    private String apellido;

    @NotBlank(message = "Ingresa tu DNI.")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos.")
    private String dni;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.")
    @Pattern(regexp = "^(?:$|(?=(?:\\D*\\d){7,})\\+?[0-9][0-9\\s().-]{5,17}[0-9])$",
            message = "Ingresa un teléfono válido, con al menos 7 dígitos.")
    private String telefono;

    @Size(max = 100, message = "La dirección no puede superar los 100 caracteres.")
    private String direccion;

    @NotNull(message = "Selecciona tu fecha de nacimiento.")
    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura.")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "Selecciona una opción de género.")
    @Pattern(regexp = "MASCULINO|FEMENINO|OTRO",
            message = "Selecciona una opción válida.")
    private String genero;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}
