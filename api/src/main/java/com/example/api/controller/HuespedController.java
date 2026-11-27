package com.example.api.controller;

import com.example.api.dto.HuespedPeticion;
import com.example.api.dto.HuespedRespuesta;
import com.example.api.service.HuespedService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * HuespedController · recurso /api/v1/huespedes
 * ------------------------------------------------------------------
 *   POST /api/v1/huespedes        -> 201 + Location · 400 · 409 (correo repetido)
 *   GET  /api/v1/huespedes/{id}   -> 200 · 404 · 400 (si el id no es número)
 *
 * Es lo MÍNIMO que necesita la reserva: crear al huésped para tener
 * su id, y consultarlo. (PUT y listado quedan para la entrega final.)
 *
 * Depende de: HuespedService y los DTOs de huésped.
 */
@RestController
@RequestMapping("/api/v1/huespedes")
public class HuespedController {

    private final HuespedService huespedService;

    public HuespedController(HuespedService huespedService) {
        this.huespedService = huespedService;
    }

    @PostMapping
    public ResponseEntity<HuespedRespuesta> crear(@Valid @RequestBody HuespedPeticion peticion) {
        HuespedRespuesta creado = huespedService.crear(peticion);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.identifier())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado); // 201 + Location
    }

    @GetMapping("/{id}")
    public HuespedRespuesta obtener(@PathVariable Long id) {
        return huespedService.obtener(id);
    }
}
