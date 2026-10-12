package com.Citas.Medicas.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Citas.Medicas.horarios.HorariosRepository;
import com.Citas.Medicas.Medico.MedicoRepository;
import com.Citas.Medicas.Paciente.PacienteRepository;

@Controller
@RequestMapping("/admin/citas")
public class CitasAdminController {

    private final CitasService service;
    private final MedicoRepository medicos;
    private final PacienteRepository pacientes;
    private final HorariosRepository horarios;

    public CitasAdminController(CitasService service,
                                MedicoRepository medicos,
                                PacienteRepository pacientes,
                                HorariosRepository horarios) {
        this.service = service;
        this.medicos = medicos;
        this.pacientes = pacientes;
        this.horarios = horarios;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("citas", service.listar());
        return "admin/citas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("medicos", medicos.listarMedicosActivos());
        model.addAttribute("pacientes", pacientes.listarActivos());
        model.addAttribute("horarios", horarios.listarHorariosActivos().stream()
                .filter(bloque -> !service.generarTurnosDentroDelBloque(bloque).isEmpty())
                .toList());
        LocalDate hoy = LocalDate.now();
        model.addAttribute("fechaMinima", hoy.toString());
        model.addAttribute("fechaMaxima", hoy.plusDays(90).toString());
        model.addAttribute("duracionConsultaMinutos", service.getDuracionConsultaMinutos());
        return "admin/citas/formulario";
    }

    @GetMapping(value = "/turnos", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<TurnoCitaRespuesta> turnos(
            @RequestParam Long idHorario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.obtenerTurnosDisponibles(idHorario, fecha).stream()
                .map(turno -> new TurnoCitaRespuesta(
                        turno.horaInicio().format(DateTimeFormatter.ofPattern("HH:mm")),
                        turno.horaFin().format(DateTimeFormatter.ofPattern("HH:mm"))))
                .toList();
    }

    @PostMapping("/guardar")
public String guardar(
         @RequestParam Long idPaciente,
         @RequestParam Long idMedico,
         @RequestParam Long idHorario,
         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
         @RequestParam(required = false)
         @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
        RedirectAttributes redirect) {

    try {

        service.registrar(
                 idPaciente,
                 idMedico,
                 idHorario,
                 fecha,
                 horaInicio
        );

        redirect.addFlashAttribute(
                "mensaje",
                "Cita registrada correctamente."
        );

        return "redirect:/admin/citas";

    } catch (IllegalArgumentException e) {

        redirect.addFlashAttribute(
                "error",
                e.getMessage()
        );

        return "redirect:/admin/citas/nuevo";
    }
}

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id,
                           RedirectAttributes redirect) {
        try {
            service.cancelar(id);
            redirect.addFlashAttribute("mensaje", "Cita cancelada");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/citas";
    }
}
