package com.Citas.Medicas.Autenticacion.Controladores;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/admin/dashboard")
    public String vistaAdmin() {
        return "admin/dashboard"; // Debes crear esta vista de prueba
    }

    @GetMapping("/medico/dashboard")
    public String vistaMedico() {
        return "medico/dashboard"; // Debes crear esta vista de prueba
    }

    @GetMapping("/paciente/dashboard")
    public String vistaPaciente() {
        return "paciente/dashboard"; // Debes crear esta vista de prueba
    }
}
