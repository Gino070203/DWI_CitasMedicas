package com.Citas.Medicas.Usuario;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioAdminService service;

    public UsuarioAdminController(UsuarioAdminService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String rol,
                         @RequestParam(required = false) Boolean estado,
                         Model model) {
        model.addAttribute("usuarios", service.filtrar(q, rol, estado));
        model.addAttribute("textoFiltro", q);
        model.addAttribute("rolFiltro", rol);
        model.addAttribute("estadoFiltro", estado);
        return "admin/usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", service.obtener(id));
        return "admin/usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam(required = false) Long idUsuario,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String dni,
            @RequestParam String correo,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String password,
            @RequestParam String rol,
            RedirectAttributes redirect) {
        try {
            service.guardar(idUsuario, nombre, apellido, dni,
                    correo, telefono, direccion, password, rol);
            redirect.addFlashAttribute("mensaje", "Usuario guardado");
            return "redirect:/admin/usuarios";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return idUsuario == null
                ? "redirect:/admin/usuarios/nuevo"
                : "redirect:/admin/usuarios/editar/" + idUsuario;
        }
    }

    @PostMapping("/estado/{id}")
    public String estado(@PathVariable Long id,
                         RedirectAttributes redirect) {
        try {
            service.cambiarEstado(id);
            redirect.addFlashAttribute("mensaje", "Estado actualizado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    
}
