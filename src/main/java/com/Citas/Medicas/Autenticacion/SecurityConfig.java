package com.Citas.Medicas.Autenticacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.Citas.Medicas.Autenticacion.Servicios.OAuth2UsuarioService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	CustomAuthenticationSuccessHandler accesoAutorizado;
	
	@Autowired
    private OAuth2UsuarioService oAuth2UsuarioService;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
            	.requestMatchers("/css/**", 
            			"/js/**", 
            			"/images/**", 
            			"/", "/login", 
            			"/registro", 
            			"/registrar", 
            			"/verificar", 
            			"/verificar-codigo").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")       // Solo para administradores
                .requestMatchers("/medico/**").hasRole("MEDICO")     // Solo para médicos
                .requestMatchers("/paciente/**").hasRole("PACIENTE") // Solo para pacientes
                .anyRequest().authenticated()                        // Todo lo demás exige login
            )
            .formLogin(form -> form
                .loginPage("/login")          // Tu vista personalizada de login (HTML)
                .successHandler(accesoAutorizado)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UsuarioService))
                    .successHandler(accesoAutorizado)
                )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
	
	
	
	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
	
	

}
