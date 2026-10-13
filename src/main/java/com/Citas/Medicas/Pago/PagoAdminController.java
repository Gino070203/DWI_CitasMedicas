
package com.Citas.Medicas.Pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Citas.Medicas.Cita.Citas;
import com.Citas.Medicas.Cita.CitasRepository;

@Controller
@RequestMapping("/admin/pagos")
public class PagoAdminController {

    private final PagoService pagoService;
    private final CitasRepository citasRepository;

    public PagoAdminController(
            PagoService pagoService,
            CitasRepository citasRepository) {

        this.pagoService = pagoService;
        this.citasRepository = citasRepository;
    }
    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String estado,
                         @RequestParam(required = false) String metodo,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         Model model) {

        model.addAttribute(
                "pagos",
                pagoService.filtrar(q, estado, metodo, desde, hasta)
        );

        model.addAttribute(
                "totalRecaudado",
                pagoService.totalRecaudado()
        );
        model.addAttribute("textoFiltro", q);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("metodoFiltro", metodo);
        model.addAttribute("desdeFiltro", desde);
        model.addAttribute("hastaFiltro", hasta);

        return "admin/pagos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        List<Citas> citasPendientes = citasRepository.findAll()
                .stream()
                .filter(c -> "PENDIENTE_PAGO"
                        .equalsIgnoreCase(c.getEstado()))
                .filter(c -> c.getPaciente() != null)
                .filter(c -> {
                    String sis = c.getPaciente().getSis();
                    boolean sinSis = sis == null || sis.trim().isEmpty();
                    boolean sisPendiente = Boolean.FALSE.equals(
                            c.getPaciente().getSisVerificado());
                    return sinSis || sisPendiente;
                })
                .toList();

        model.addAttribute("citas", citasPendientes);

        return "admin/pagos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam Long idCita,
            @RequestParam BigDecimal monto,
            @RequestParam String metodoPago,
            RedirectAttributes redirect) {

        try {

            pagoService.registrar(
                    idCita,
                    monto,
                    metodoPago
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "Pago registrado correctamente. " +
                    "La cita médica fue confirmada."
            );

            return "redirect:/admin/pagos";

        } catch (IllegalArgumentException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/pagos/nuevo";
        }
    }
}
