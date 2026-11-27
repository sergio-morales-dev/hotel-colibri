package com.example.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * HabitacionEstadoPeticion · cuerpo de PATCH /api/v1/habitaciones/{numero}
 * ------------------------------------------------------------------
 * Ejemplo:  { "activa": false }
 *
 * PATCH = modificación PARCIAL: solo cambia este campo y nada más.
 * Es la forma de "dar de baja" una habitación, porque las habitaciones
 * con reservas no se borran, se desactivan (regla 6.5).
 *
 * Se usa Boolean (objeto) y no boolean (primitivo): si el cliente no
 * manda el campo, llega null y @NotNull responde 400. Con boolean
 * llegaría false sin avisar y desactivaría la habitación por error.
 */
public record HabitacionEstadoPeticion(

        @NotNull(message = "es obligatorio (true o false)")
        Boolean activa
) {
}
