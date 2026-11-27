package com.example.api.controller;

import com.example.api.dto.ReservaPeticion;
import com.example.api.dto.ReservaRespuesta;
import com.example.api.service.ReservaService;
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
 * ReservaController · recurso /api/v1/reservas
 * ------------------------------------------------------------------
 *   POST /api/v1/reservas        -> 201 + Location, estado PENDIENTE
 *                                   · 400 datos/fechas inválidos
 *                                   · 404 huésped o habitación no existe
 *                                   · 409 habitación ocupada en esas fechas
 *                                   · 422 capacidad excedida o habitación inactiva
 *   GET  /api/v1/reservas/{id}   -> 200 · 404
 *
 * (La cotización GET /api/v1/reservas/{id}/cotizacion la hace Jaqueline
 *  en su propio CotizacionController; pagar, cancelar, llegada y salida
 *  son de la entrega final.)
 *
 * Depende de: ReservaService y los DTOs de reserva.
 */
@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaRespuesta> crear(@Valid @RequestBody ReservaPeticion peticion) {
        ReservaRespuesta creada = reservaService.crear(peticion);
        // El folio es "RES-1"; la URI usa solo el número: /api/v1/reservas/1
        String id = creada.reservationId().substring(ReservaRespuesta.PREFIJO_FOLIO.length());
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada); // 201 + Location
    }

    @GetMapping("/{id}")
    public ReservaRespuesta obtener(@PathVariable Long id) {
        return reservaService.obtener(id);
    }
}
