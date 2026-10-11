package com.Citas.Medicas.Autenticacion.Controladores;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Citas.Medicas.Autenticacion.Servicios.CorreoService;
import com.Citas.Medicas.Roles.Rol;
import com.Citas.Medicas.Roles.RolRepository;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

@Controller
public class RegistroController {
	
	@Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CorreoService correoService;
    
    
    // 1. Mostrar la vista de registro
    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro"; // Ruta de tu archivo html de registro
    }
    
    // 2. Procesar el formulario de registro
    
    @PostMapping("/registrar")
    public String registrarUsuario(@ModelAttribute Usuario usuario, Model model) {
        // Verificar si el correo ya existe
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            return "auth/registro";
        }

        // Asignar rol por defecto (PACIENTE)
        Rol rolPaciente = rolRepository.findByNombre("PACIENTE")
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre("PACIENTE");
                    return rolRepository.save(nuevoRol);
                });
        usuario.setRol(rolPaciente);

        // Cifrar la contraseña
        usuario.setContraseña(passwordEncoder.encode(usuario.getContraseña()));

        // Configurar estado inactivo hasta que verifique el correo
        usuario.setEstado(false);

        // Generar un código aleatorio de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoVerificacion(codigo);

        // Guardar en la base de datos
        usuarioRepository.save(usuario);

        // Enviar el correo electrónico con el código
        correoService.enviarCodigoVerificacion(usuario.getCorreo(), codigo);

        // Redirigir a la vista donde ingresará el código (pasando el correo como parámetro)
        return "redirect:/verificar?correo=" + usuario.getCorreo();
    }
    
    // 3. Mostrar la vista para introducir el código de verificación
    @GetMapping("/verificar")
    public String mostrarVistaVerificacion(@RequestParam("correo") String correo, Model model) {
        model.addAttribute("correo", correo);
        return "auth/verificar"; // Ruta de tu archivo html de verificación
    }
    
    
    // 4. Procesar y validar el código ingresado
    @PostMapping("/verificar-codigo")
    public String verificarCodigo(@RequestParam("correo") String correo, 
                                  @RequestParam("codigo") String codigoIngresado, 
                                  Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

        if (usuario != null && usuario.getCodigoVerificacion() != null && usuario.getCodigoVerificacion().equals(codigoIngresado)) {
            // Código correcto: activamos la cuenta y limpiamos el código temporal
            usuario.setEstado(true);
            usuario.setCodigoVerificacion(null);
            usuarioRepository.save(usuario);

            return "redirect:/login?verificado=true";
        } else {
            // Código incorrecto
            model.addAttribute("error", "Código de verificación incorrecto. Inténtalo de nuevo.");
            model.addAttribute("correo", correo);
            return "auth/verificar";
        }
    }
    
    
    
    
    
    
    
    
    
    

}
