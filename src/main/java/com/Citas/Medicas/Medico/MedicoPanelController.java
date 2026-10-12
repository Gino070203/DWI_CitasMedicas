package com.Citas.Medicas.Medico;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medico")
public class MedicoPanelController {

    private final MedicoPanelService service;

    public MedicoPanelController(MedicoPanelService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        try {
            model.addAttribute("medico", service.medicoActual(auth.getName()));
            model.addAttribute("hoy", service.agendaDeHoy(auth.getName()));
            model.addAttribute("porAtender", service.porAtender(auth.getName()));
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "medico/dashboard";
    }

    @GetMapping("/citas/{id}/diagnostico")
    public String formDiagnostico(@PathVariable Long id, Authentication auth,
                                  Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("cita", service.obtenerCita(auth.getName(), id));
            return "medico/diagnostico";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/medico/dashboard";
        }
    }

    @PostMapping("/citas/{id}/diagnostico")
    public String guardarDiagnostico(@PathVariable Long id,
                                     @RequestParam String diagnostico,
                                     Authentication auth,
                                     RedirectAttributes redirect) {
        try {
            service.registrarDiagnostico(auth.getName(), id, diagnostico);
            redirect.addFlashAttribute("mensaje", "Diagnóstico registrado");
            return "redirect:/medico/dashboard";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/medico/citas/" + id + "/diagnostico";
        }
    }
}