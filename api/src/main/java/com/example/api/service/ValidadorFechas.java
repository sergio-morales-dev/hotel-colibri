package com.example.api.service;

import com.example.api.exception.DatosInvalidosException;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * ValidadorFechas · regla 6.3 en UN solo lugar
 * ------------------------------------------------------------------
 *  - La fecha de salida debe ser posterior a la de entrada.
 *  - La fecha de entrada no puede estar en el pasado.
 *  Si no se cumple -> DatosInvalidosException (400).
 *
 * La usan DOS servicios: ReservaService (al crear una reserva) y
 * HabitacionService (al buscar habitaciones disponibles). Por eso vive
 * aparte: si la regla cambia, se cambia aquí y aplica en ambos.
 *
 * Es una clase de utilidad: "final" (nadie hereda de ella), constructor
 * privado (nadie la instancia) y métodos static.
 *
 * Depende de: DatosInvalidosException.
 */
public final class ValidadorFechas {

    // "Hoy" según la zona horaria del hotel, no la del servidor.
    private static final ZoneId ZONA_HOTEL = ZoneId.of("America/Mexico_City");

    private ValidadorFechas() {
    }

    public static void validarEstancia(LocalDate entrada, LocalDate salida) {
        if (!salida.isAfter(entrada)) {
            throw new DatosInvalidosException("La fecha de salida (" + salida
                    + ") debe ser posterior a la fecha de entrada (" + entrada + ")");
        }
        LocalDate hoy = LocalDate.now(ZONA_HOTEL);
        if (entrada.isBefore(hoy)) {
            throw new DatosInvalidosException("La fecha de entrada (" + entrada
                    + ") no puede estar en el pasado (hoy es " + hoy + ")");
        }
    }
}
