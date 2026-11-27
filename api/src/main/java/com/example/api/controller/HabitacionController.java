package com.example.api.controller;

import com.example.api.dto.HabitacionEstadoPeticion;
import com.example.api.dto.HabitacionPeticion;
import com.example.api.dto.HabitacionRespuesta;
import com.example.api.service.HabitacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * HabitacionController · recurso /api/v1/habitaciones
 * ------------------------------------------------------------------
 *   GET    /api/v1/habitaciones            -> 200 lista
 *   GET    /api/v1/habitaciones/{numero}   -> 200 · 404
 *   POST   /api/v1/habitaciones            -> 201 + Location · 400 · 409
 *   PUT    /api/v1/habitaciones/{numero}   -> 200 · 400 · 404
 *   PATCH  /api/v1/habitaciones/{numero}   -> 200 · 400 · 404  (activar/desactivar)
 * (El filtro ?entrada=&salida=&huespedes= se agrega en la fase 3.)
 *
 * El controller es "delgado": traduce HTTP <-> Java y delega TODO al
 * service. No tiene reglas de negocio ni try/catch (los errores los
 * atiende ManejadorErrores).
 *
 * @Valid: revisa las anotaciones del DTO (@NotBlank, @Min...) ANTES
 * de entrar al método; si algo falla, responde 400.
 *
 * Depende de: HabitacionService y los DTOs de habitación.
 */
@RestController
@RequestMapping("/api/v1/habitaciones")
public class HabitacionController {

    private final HabitacionService habitacionService;

    public HabitacionController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @GetMapping
    public List<HabitacionRespuesta> listar() {
        return habitacionService.listar();
    }

    @GetMapping("/{numero}")
    public HabitacionRespuesta obtener(@PathVariable String numero) {
        return habitacionService.obtener(numero);
    }

    @PostMapping
    public ResponseEntity<HabitacionRespuesta> crear(@Valid @RequestBody HabitacionPeticion peticion) {
        HabitacionRespuesta creada = habitacionService.crear(peticion);
        // Location: la URL del recurso recién creado, p. ej.
        // http://localhost:8080/api/v1/habitaciones/301
        // Se arma a partir de la URL de esta petición + "/{numero}".
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numero}")
                .buildAndExpand(creada.identifier())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada); // 201 Created
    }

    @PutMapping("/{numero}")
    public HabitacionRespuesta actualizar(@PathVariable String numero,
                                          @Valid @RequestBody HabitacionPeticion peticion) {
        return habitacionService.actualizar(numero, peticion);
    }

    @PatchMapping("/{numero}")
    public HabitacionRespuesta cambiarEstado(@PathVariable String numero,
                                             @Valid @RequestBody HabitacionEstadoPeticion peticion) {
        return habitacionService.cambiarEstado(numero, peticion.activa());
    }
}
