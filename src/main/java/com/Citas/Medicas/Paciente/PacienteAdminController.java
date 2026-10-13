package com.Citas.Medicas.Paciente;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Citas.Medicas.Usuario.UsuarioRepository;

@Controller
@RequestMapping("/admin/pacientes")
public class PacienteAdminController {

    private final PacienteService service;
    private final UsuarioRepository usuarios;

    public PacienteAdminController(PacienteService service,
                                   UsuarioRepository usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Boolean estado,
                         @RequestParam(required = false) Boolean conSis,
                         Model model) {
        model.addAttribute("pacientes", service.filtrar(q, estado, conSis));
        model.addAttribute("textoFiltro", q);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("conSisFiltro", conSis);
        return "admin/pacientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuarios", usuarios.buscarPorRol("PACIENTE"));
        return "admin/pacientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long idUsuario,
                          @RequestParam String numeroHistoria,
                          @RequestParam(required = false) String sis,
                          @RequestParam
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                          LocalDate fechaNacimiento,
                          @RequestParam String genero,
                          @RequestParam(required = false) String direccion,
                          RedirectAttributes redirect) {
        try {
            service.registrar(idUsuario, numeroHistoria, sis,
                    fechaNacimiento, genero, direccion);
            redirect.addFlashAttribute("mensaje", "Paciente registrado");
            return "redirect:/admin/pacientes";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/pacientes/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", service.obtener(id));
        return "admin/pacientes/editar";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam String numeroHistoria,
                             @RequestParam(required = false) String sis,
                             @RequestParam
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                             LocalDate fechaNacimiento,
                             @RequestParam String genero,
                             @RequestParam(required = false) String direccion,
                             @RequestParam(defaultValue = "false") boolean numeroHistoriaVerificado,
                             @RequestParam(defaultValue = "false") boolean sisVerificado,
                             RedirectAttributes redirect) {
        try {
            service.actualizar(id, numeroHistoria, sis,
                    fechaNacimiento, genero, direccion,
                    numeroHistoriaVerificado, sisVerificado);
            redirect.addFlashAttribute("mensaje", "Paciente actualizado");
            return "redirect:/admin/pacientes";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/pacientes/editar/" + id;
        }
    }

    @PostMapping("/estado/{id}")
    public String estado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return "redirect:/admin/pacientes";
    }
}
