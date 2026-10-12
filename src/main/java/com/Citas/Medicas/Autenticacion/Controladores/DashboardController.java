
package com.Citas.Medicas.Autenticacion.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // ==========================================
    // DASHBOARD DEL MÉDICO
    // ==========================================
    @GetMapping("/medico/dashboard")
    public String vistaMedico() {
        return "medico/dashboard";
    }


}
