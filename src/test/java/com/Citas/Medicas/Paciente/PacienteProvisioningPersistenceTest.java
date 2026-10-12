package com.Citas.Medicas.Paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.Citas.Medicas.Roles.Rol;
import com.Citas.Medicas.Roles.RolRepository;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioRepository;

@SpringBootTest
@Transactional
class PacienteProvisioningPersistenceTest {

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PacienteRepository pacientes;

    @Autowired
    private RolRepository roles;

    @Autowired
    private PacienteProvisioningService provisioning;

    @Test
    void persistePerfilMinimoEnMySqlSinInventarDatosClinicos() {
        Rol rol = roles.findByNombre("PACIENTE").orElseThrow();
        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setNombre("Paciente");
        usuario.setApellido("Prueba");
        usuario.setCorreo("provision-" + UUID.randomUUID() + "@example.invalid");
        usuario.setDni(String.format("%08d",
                Math.floorMod(System.nanoTime(), 100_000_000L)));
        usuario.setContraseña("hash-de-prueba");
        usuario.setEstado(true);
        usuario = usuarios.saveAndFlush(usuario);

        Paciente creado = provisioning.asegurarPerfilPaciente(usuario.getIdUsuario());
        pacientes.flush();

        Paciente recuperado = pacientes.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow();
        assertNotNull(creado.getIdPaciente());
        assertEquals(usuario.getIdUsuario(), recuperado.getUsuario().getIdUsuario());
        assertEquals(true, recuperado.getEstado());
        assertNull(recuperado.getNumeroHistoria());
        assertNull(recuperado.getSis());
        assertNull(recuperado.getFechaNacimiento());
        assertNull(recuperado.getGenero());
    }
}
