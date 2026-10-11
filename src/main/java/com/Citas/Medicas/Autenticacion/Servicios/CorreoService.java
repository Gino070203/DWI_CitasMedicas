package com.Citas.Medicas.Autenticacion.Servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {
	
	@Autowired
    private JavaMailSender mailSender;
	
	public void enviarCodigoVerificacion(String destinatario, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Código de Verificación - Centro Médico");
        mensaje.setText("Hola, tu código de verificación de 6 dígitos es: " + codigo + 
                        "\nPor favor, ingrésalo en la plataforma para activar tu cuenta.");
        mailSender.send(mensaje);
    }

}
