package com.Citas.Medicas.Autenticacion.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final DashboardService service;

    public AdminDashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("totalUsuarios", service.totalUsuarios());
        model.addAttribute("totalMedicos", service.totalMedicos());
        model.addAttribute("totalPacientes", service.totalPacientes());
        model.addAttribute("totalEspecialidades",
                service.totalEspecialidades());
        model.addAttribute("totalCitas", service.totalCitas());
        model.addAttribute("citasHoy", service.citasHoy());
        model.addAttribute("totalPagos", service.totalPagos());

        return "admin/dashboard";
    }
}