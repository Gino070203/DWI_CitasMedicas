package com.Citas.Medicas.Especialidad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/especialidades")
public class EspecialidadAdminController {

    private final EspecialidadService service;

    public EspecialidadAdminController(EspecialidadService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("especialidades", service.listar());
        return "admin/especialidades/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("especialidad", new Especialidad());
        return "admin/especialidades/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("especialidad", service.obtener(id));
        return "admin/especialidades/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam(required = false) Long idEspecialidad,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            RedirectAttributes redirect) {
        try {
            service.guardar(idEspecialidad, nombre, descripcion);
            redirect.addFlashAttribute("mensaje", "Especialidad guardada");
            return "redirect:/admin/especialidades";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return idEspecialidad == null
                ? "redirect:/admin/especialidades/nuevo"
                : "redirect:/admin/especialidades/editar/" + idEspecialidad;
        }
    }

    @PostMapping("/estado/{id}")
    public String estado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return "redirect:/admin/especialidades";
    }
}