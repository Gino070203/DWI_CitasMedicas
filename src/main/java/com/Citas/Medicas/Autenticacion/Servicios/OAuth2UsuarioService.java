package com.Citas.Medicas.Autenticacion.Servicios;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.Citas.Medicas.Roles.Rol;
import com.Citas.Medicas.Roles.RolRepository;
import com.Citas.Medicas.Paciente.PacienteProvisioningService;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class OAuth2UsuarioService extends DefaultOAuth2UserService{
	
	@Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PacienteProvisioningService pacienteProvisioningService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Obtenemos los datos del usuario desde Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String correo = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("given_name");
        String apellido = oAuth2User.getAttribute("family_name");

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElseGet(() -> {
            Rol rolPaciente = rolRepository.findByNombre("PACIENTE")
                    .orElseGet(() -> {
                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombre("PACIENTE");
                        return rolRepository.save(nuevoRol);
                    });

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setCorreo(correo);
            nuevoUsuario.setNombre(nombre != null ? nombre : "Usuario");
            nuevoUsuario.setApellido(apellido != null ? apellido : "Google");
            nuevoUsuario.setContraseña("");
            nuevoUsuario.setEstado(true);
            nuevoUsuario.setRol(rolPaciente);
            return usuarioRepository.save(nuevoUsuario);
        });

        if (usuario.getRol() != null
                && "PACIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            pacienteProvisioningService.asegurarPerfilPaciente(usuario.getIdUsuario());
        }
        
     // 3. Definimos explícitamente el rol que Spring Security debe leer para este usuario de Google
        String nombreRol = usuario.getRol().getNombre(); // "PACIENTE"
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + nombreRol)
        );

     // 4. Retornamos un DefaultOAuth2User que incluye las autoridades/roles correctos
        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "email" // Atributo clave que identifica de manera única al usuario
        );
    }

}
