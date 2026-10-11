package com.Citas.Medicas.Roles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RolService {
	
	@Autowired
	RolRepository rolRepository;
	
	public void agregarRol(String nombre) {
		Rol rol = new Rol(nombre);
		rolRepository.save(rol);
	}

}
