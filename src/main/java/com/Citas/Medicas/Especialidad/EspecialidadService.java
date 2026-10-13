package com.Citas.Medicas.Especialidad;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EspecialidadService {

    private final EspecialidadRepository repository;

    public EspecialidadService(EspecialidadRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Especialidad> listar() {
        return repository.listarTodas();
    }

    @Transactional(readOnly = true)
    public List<Especialidad> filtrar(String texto, Boolean estado) {
        String criterio = texto == null || texto.isBlank() ? null : texto.trim();
        return repository.filtrarEspecialidades(criterio, estado);
    }

    @Transactional(readOnly = true)
    public List<Especialidad> activas() {
        return repository.listarActivas();
    }

    @Transactional(readOnly = true)
    public Especialidad obtener(Long id) {
        return repository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Especialidad no encontrada"));
    }

    @Transactional
    public Especialidad guardar(Long id, String nombre,
                                String descripcion) {

        if (nombre == null || nombre.isBlank()
                || nombre.trim().length() > 100) {
            throw new IllegalArgumentException("Nombre inválido");
        }

        if (descripcion != null && descripcion.length() > 200) {
            throw new IllegalArgumentException(
                "La descripción supera 200 caracteres");
        }

        String limpio = nombre.trim();

        repository.buscarNombreExacto(limpio).ifPresent(e -> {
            if (id == null || !e.getIdEspecialidad().equals(id)) {
                throw new IllegalArgumentException(
                    "La especialidad ya existe");
            }
        });

        Especialidad e = id == null
            ? new Especialidad() : obtener(id);

        e.setNombre(limpio);
        e.setDescripcion(descripcion);

        if (id == null) {
            e.setEstado(true);
        }

        return repository.save(e);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Especialidad e = obtener(id);
        e.setEstado(!Boolean.TRUE.equals(e.getEstado()));
        repository.save(e);
    }
}
