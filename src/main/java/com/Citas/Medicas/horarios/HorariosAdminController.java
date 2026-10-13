package com.Citas.Medicas.horarios;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Citas.Medicas.Medico.MedicoRepository;

@Controller
@RequestMapping("/admin/horarios")
public class HorariosAdminController {

    private final HorariosService service;
    private final MedicoRepository medicos;

    public HorariosAdminController(
            HorariosService service, MedicoRepository medicos) {
        this.service = service;
        this.medicos = medicos;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String diaSemana,
                         @RequestParam(required = false) Boolean estado,
                         Model model) {
        List<Horarios> bloques = service.filtrar(q, diaSemana, estado);
        model.addAttribute("horarios", bloques);
        model.addAttribute("turnosPorBloque", bloques.stream()
                .collect(Collectors.toMap(Horarios::getIdHorario,
                        service::contarTurnosPosibles)));
        model.addAttribute("duracionConsultaMinutos", service.getDuracionConsultaMinutos());
        model.addAttribute("textoFiltro", q);
        model.addAttribute("diaFiltro", diaSemana);
        model.addAttribute("estadoFiltro", estado);
        return "admin/horarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("medicos", medicos.listarMedicosActivos());
        model.addAttribute("duracionConsultaMinutos", service.getDuracionConsultaMinutos());
        return "admin/horarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam(required = false) Long idHorario,
            @RequestParam Long idMedico,
            @RequestParam String diaSemana,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaFin,
            RedirectAttributes redirect) {
        try {
            service.guardar(idHorario, idMedico,
                    diaSemana, horaInicio, horaFin);
            redirect.addFlashAttribute("mensaje", "Horario guardado");
            return "redirect:/admin/horarios";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/horarios/nuevo";
        }
    }

    @PostMapping("/estado/{id}")
    public String estado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return "redirect:/admin/horarios";
    }
}
