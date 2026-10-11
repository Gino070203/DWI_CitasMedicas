package com.Citas.Medicas.Roles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("/rol")
public class RolController {
	
	@Autowired
	RolService rolservicio;
	
	@PostMapping
	public void agregarRol(String nombreRol){
		rolservicio.agregarRol(nombreRol);
	}
	
	

}
