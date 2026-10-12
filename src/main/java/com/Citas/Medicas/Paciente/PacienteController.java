package com.Citas.Medicas.Paciente;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import com.Citas.Medicas.Cita.Citas;
import com.Citas.Medicas.Cita.CitasService;
import com.Citas.Medicas.Cita.TurnoCitaRespuesta;
import com.Citas.Medicas.Especialidad.Especialidad;
import com.Citas.Medicas.Especialidad.EspecialidadRepository;
import com.Citas.Medicas.Horarios.Horarios;
import com.Citas.Medicas.Usuario.Usuario;
import com.Citas.Medicas.Usuario.UsuarioActualService;

@Controller
@RequestMapping("/paciente")
public class PacienteController {

	@Autowired
    private PacienteService pacienteService;

    @Autowired
    private UsuarioActualService usuarioActualService;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private CitasService citasService;

    @Autowired
    private PerfilPacienteService perfilPacienteService;

    @Autowired
    private PacienteProvisioningService pacienteProvisioningService;

    @ModelAttribute("nombrePaciente")
    public String agregarNombrePaciente(Authentication authentication) {
        return usuarioActualService.buscarUsuarioActual(authentication)
                .map(Usuario::getNombre)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .orElse("Paciente");
    }

    // 	1. Panel principal del paciente
    @GetMapping("/dashboard")
    public String dashboardPaciente() {
        return "paciente/dashboard"; // Vista principal del paciente
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(defaultValue = "false") boolean completar) {
        try {
            Long idUsuario = idUsuarioActual(authentication);
            // Reparación idempotente para cuentas PACIENTE creadas antes de
            // que el alta de perfiles quedara integrada.
            pacienteProvisioningService.asegurarPerfilPaciente(idUsuario);
            prepararPerfil(idUsuario, null, model);
            if (completar) {
                model.addAttribute("perfilIncompleto",
                        "Completa tus datos personales antes de reservar una cita.");
            }
            return "paciente/perfil";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorPerfil", e.getMessage());
            return "redirect:/paciente/dashboard";
        }
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(
            Authentication authentication,
            @Valid @ModelAttribute("formularioPerfil") PerfilPacienteForm formulario,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long idUsuario = idUsuarioActual(authentication);
        if (bindingResult.hasErrors()) {
            prepararPerfil(idUsuario, formulario, model);
            return "paciente/perfil";
        }

        try {
            perfilPacienteService.actualizarPerfil(idUsuario, formulario);
            redirectAttributes.addFlashAttribute(
                    "perfilActualizado", "Tus datos se actualizaron correctamente.");
            return "redirect:/paciente/perfil";
        } catch (DniPacienteDuplicadoException e) {
            bindingResult.rejectValue("dni", "dni.duplicado", e.getMessage());
            prepararPerfil(idUsuario, formulario, model);
            return "paciente/perfil";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("perfil.invalido", e.getMessage());
            prepararPerfil(idUsuario, formulario, model);
            return "paciente/perfil";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorPerfil", e.getMessage());
            return "redirect:/paciente/dashboard";
        }
    }

    private Long idUsuarioActual(Authentication authentication) {
        return usuarioActualService.buscarUsuarioActual(authentication)
                .map(Usuario::getIdUsuario)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo identificar tu cuenta. Vuelve a iniciar sesión."));
    }

    private void prepararPerfil(Long idUsuario, PerfilPacienteForm formulario, Model model) {
        PerfilPacienteVista vista = perfilPacienteService.obtenerPerfil(idUsuario);
        model.addAttribute("perfilVista", vista);
        model.addAttribute("formularioPerfil",
                formulario != null ? formulario : vista.formulario());
        model.addAttribute("fechaMaximaNacimiento", LocalDate.now().toString());
    }

    // 2. Vista para buscar horarios y reservar citas (Muestra especialidades)
    @GetMapping("/reservar")
    public String mostrarFormularioReserva(@RequestParam(required = false) Long idEspecialidad, Model model) {
        List<Especialidad> especialidades = especialidadRepository.findAll();
        model.addAttribute("especialidades", especialidades);

        if (idEspecialidad != null) {
            List<Horarios> horariosDisponibles = pacienteService.obtenerHorariosPorEspecialidad(idEspecialidad);
            model.addAttribute("horarios", horariosDisponibles);
            model.addAttribute("especialidadSeleccionada", idEspecialidad);

            LocalDate desde = LocalDate.now();
            LocalDate hasta = desde.plusDays(90);
            model.addAttribute("fechaMinimaCalendario", desde.toString());
            model.addAttribute("fechaMaximaCalendario", hasta.toString());
            model.addAttribute("fechasDisponiblesPorHorario",
                    citasService.obtenerFechasDisponiblesPorHorario(
                            horariosDisponibles, desde, hasta));
            model.addAttribute("duracionConsultaMinutos",
                    citasService.getDuracionConsultaMinutos());
        }

        return "paciente/reservar"; // Vista HTML de reserva
    }

    @GetMapping(value = "/reservar/turnos", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<TurnoCitaRespuesta> obtenerTurnosDisponibles(
            @RequestParam Long idHorario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citasService.obtenerTurnosDisponibles(idHorario, fecha).stream()
                .map(turno -> new TurnoCitaRespuesta(
                        turno.horaInicio().format(DateTimeFormatter.ofPattern("HH:mm")),
                        turno.horaFin().format(DateTimeFormatter.ofPattern("HH:mm"))))
                .toList();
    }

    // 3. Procesar la reserva de la cita (Usa el servicio transaccional con validación de SIS)
    @PostMapping("/reservar")
    public String confirmarReserva(Authentication authentication,
                                   @RequestParam("idHorario") Long idHorario,
                                   @RequestParam(required = false) Long idEspecialidad,
                                   @RequestParam("fechaCita") LocalDate fechaCita,
                                   @RequestParam(value = "horaInicio", required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                                   LocalTime horaInicio,
                                   Model model) {
        try {
            Usuario usuario = usuarioActualService.buscarUsuarioActual(authentication).orElseThrow();
            pacienteProvisioningService.asegurarPerfilPaciente(usuario.getIdUsuario());
            Paciente paciente = pacienteService.obtenerPacientePorUsuarioId(usuario.getIdUsuario());

            if (paciente == null) {
                model.addAttribute("error", "Perfil de paciente no encontrado.");
                return redirigirAReserva(idEspecialidad, "NoPaciente");
            }

            // Ejecutamos la lógica transaccional de reserva y validación de SIS
            Citas citaReservada = pacienteService.reservarCita(
                    paciente.getIdPaciente(), idHorario, fechaCita, horaInicio);

            // Redirigir al historial con mensaje según estado (SIS vs Pago Presencial)
            if ("CONFIRMADA".equals(citaReservada.getEstado())) {
                return "redirect:/paciente/citas?exito=sis";
            } else {
                return "redirect:/paciente/citas?exito=pendiente";
            }

        } catch (PerfilPacienteIncompletoException e) {
            return "redirect:/paciente/perfil?completar=true";
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la reserva: " + e.getMessage());
            return redirigirAReserva(idEspecialidad, "Excepcion");
        }
    }

    private String redirigirAReserva(Long idEspecialidad, String error) {
        if (idEspecialidad == null) {
            return "redirect:/paciente/reservar?error=" + error;
        }
        return "redirect:/paciente/reservar?idEspecialidad="
                + idEspecialidad + "&error=" + error;
    }

    // 4. Ver el historial de citas del paciente
    @GetMapping("/citas")
    public String listarHistorialCitas(Authentication authentication, Model model,
                                       @RequestParam(required = false) String exito) {
        Usuario usuario = usuarioActualService.buscarUsuarioActual(authentication).orElse(null);
        if (usuario != null) {
            pacienteProvisioningService.asegurarPerfilPaciente(usuario.getIdUsuario());
            Paciente paciente = pacienteService.obtenerPacientePorUsuarioId(usuario.getIdUsuario());
            if (paciente != null) {
                List<Citas> historial = pacienteService.obtenerHistorialCitas(paciente.getIdPaciente());
                model.addAttribute("citas", historial);
            }
        }
        model.addAttribute("exito", exito);
        return "paciente/citas"; // Vista HTML de historial
    }

}
