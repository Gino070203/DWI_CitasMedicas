package com.Citas.Medicas.Medico;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Citas.Medicas.Especialidad.EspecialidadRepository;
import com.Citas.Medicas.Usuario.UsuarioRepository;

@Controller
@RequestMapping("/admin/medicos")
public class MedicoAdminController {

    private final MedicoService service;
    private final UsuarioRepository usuarios;
    private final EspecialidadRepository especialidades;

    public MedicoAdminController(
            MedicoService service,
            UsuarioRepository usuarios,
            EspecialidadRepository especialidades) {
        this.service = service;
        this.usuarios = usuarios;
        this.especialidades = especialidades;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Long idEspecialidad,
                         @RequestParam(required = false) Boolean estado,
                         Model model) {
        model.addAttribute("medicos", service.filtrar(q, idEspecialidad, estado));
        model.addAttribute("especialidadesFiltro", especialidades.listarTodas());
        model.addAttribute("textoFiltro", q);
        model.addAttribute("especialidadFiltro", idEspecialidad);
        model.addAttribute("estadoFiltro", estado);
        return "admin/medicos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuarios", usuarios.buscarPorRol("MEDICO"));
        model.addAttribute("especialidades", especialidades.listarActivas());
        return "admin/medicos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long idUsuario,
                          @RequestParam Long idEspecialidad,
                          @RequestParam String cmp,
                          RedirectAttributes redirect) {
        try {
            service.registrar(idUsuario, idEspecialidad, cmp);
            redirect.addFlashAttribute("mensaje", "Médico registrado");
            return "redirect:/admin/medicos";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/medicos/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("medico", service.obtener(id));
        model.addAttribute("especialidades", especialidades.listarActivas());
        return "admin/medicos/editar";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam Long idEspecialidad,
                             @RequestParam String cmp,
                             RedirectAttributes redirect) {
        try {
            service.actualizar(id, idEspecialidad, cmp);
            redirect.addFlashAttribute("mensaje", "Médico actualizado");
            return "redirect:/admin/medicos";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/medicos/editar/" + id;
        }
    }

    @PostMapping("/estado/{id}")
    public String estado(@PathVariable Long id) {
        service.cambiarEstado(id);
        return "redirect:/admin/medicos";
    }
}
