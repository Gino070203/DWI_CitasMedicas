package com.Citas.Medicas.Autenticacion.Servicios;

import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
		 // 1. Buscamos al usuario en la BD por su correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + correo));

        // 2. Extraemos el rol desde tu entidad Rol (relacionada en Usuario)
        // Spring Security exige que los roles comiencen con el prefijo "ROLE_"
        String nombreRol = usuario.getRol().getNombre(); // Ej: "ADMIN", "MEDICO", "PACIENTE"
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + nombreRol)
        );

        // 3. Retornamos el objeto que Spring Security procesa para validar credenciales
        return new User(
                usuario.getCorreo(),
                usuario.getContraseña(), 
                authorities
        );
	}
}
