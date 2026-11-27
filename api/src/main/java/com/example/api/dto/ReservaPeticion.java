package com.example.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * ReservaPeticion · cuerpo de POST /api/v1/reservas
 * ------------------------------------------------------------------
 * Ejemplo (prueba 2 del enunciado):
 *   { "huesped": 1, "habitacion": "102",
 *     "fechaEntrada": "2027-01-15", "fechaSalida": "2027-01-18",
 *     "numHuespedes": 2 }
 *
 * - huesped: el id del huésped (lo devuelve POST /api/v1/huespedes).
 * - habitacion: el NÚMERO de la habitación ("102"), igual que en la URI.
 * - Las fechas van como AAAA-MM-DD y Spring las convierte a LocalDate.
 * - NO hay campo de precio ni de total: el cliente nunca envía el
 *   precio; lo calcula el servidor (regla 6.5). Si alguien manda
 *   "total": 1, simplemente se ignora.
 *
 * Aquí solo se valida cada campo POR SEPARADO (que exista, que sea
 * positivo...). Las reglas que comparan campos o consultan la BD
 * (salida después de entrada, capacidad, traslape) viven en
 * ReservaService.
 */
public record ReservaPeticion(

        @NotNull(message = "es obligatorio (id del huésped)")
        @Positive(message = "debe ser un id válido")
        Long huesped,

        @NotBlank(message = "es obligatoria (número de habitación)")
        String habitacion,

        @NotNull(message = "es obligatoria (AAAA-MM-DD)")
        LocalDate fechaEntrada,

        @NotNull(message = "es obligatoria (AAAA-MM-DD)")
        LocalDate fechaSalida,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser al menos 1")
        @Max(value = 10, message = "no puede ser mayor que 10")
        Integer numHuespedes
) {
}
