package com.example.api.controller;

import com.example.api.dto.HabitacionEstadoPeticion;
import com.example.api.dto.HabitacionPeticion;
import com.example.api.dto.HabitacionRespuesta;
import com.example.api.exception.DatosInvalidosException;
import com.example.api.service.HabitacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * HabitacionController · recurso /api/v1/habitaciones
 * ------------------------------------------------------------------
 *   GET    /api/v1/habitaciones            -> 200 lista
 *   GET    /api/v1/habitaciones?entrada=&salida=&huespedes=  -> 200 disponibles · 400
 *   GET    /api/v1/habitaciones/{numero}   -> 200 · 404
 *   POST   /api/v1/habitaciones            -> 201 + Location · 400 · 409
 *   PUT    /api/v1/habitaciones/{numero}   -> 200 · 400 · 404
 *   PATCH  /api/v1/habitaciones/{numero}   -> 200 · 400 · 404  (activar/desactivar)
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

    /**
     * Sin parámetros -> todas las habitaciones.
     * Con ?entrada=2027-01-15&salida=2027-01-18&huespedes=2 -> solo las
     * DISPONIBLES para esa estancia (prueba 14). Es el MISMO recurso
     * (habitaciones) con un filtro, por eso usa la misma URI con query
     * params en lugar de una URI nueva como /disponibles.
     *
     * @DateTimeFormat(iso = DATE): las fechas llegan como AAAA-MM-DD.
     * Si llega "abc" o un número inválido, Spring responde 400 solo.
     */
    @GetMapping
    public List<HabitacionRespuesta> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entrada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
            @RequestParam(required = false) @Min(value = 1, message = "debe ser al menos 1") Integer huespedes) {

        if (entrada == null && salida == null && huespedes == null) {
            return habitacionService.listar();
        }
        if (entrada == null || salida == null || huespedes == null) {
            throw new DatosInvalidosException(
                    "Para buscar habitaciones disponibles envíe los tres parámetros: entrada, salida y huespedes");
        }
        return habitacionService.disponibles(entrada, salida, huespedes);
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
