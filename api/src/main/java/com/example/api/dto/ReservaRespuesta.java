package com.example.api.dto;

import com.example.api.model.Habitacion;
import com.example.api.model.Huesped;
import com.example.api.model.Reserva;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ReservaRespuesta · lo que la API devuelve de una reserva
 * ------------------------------------------------------------------
 * JSON-LD con schema.org, tipo LodgingReservation (glosario del equipo):
 * {
 *   "@context": "https://schema.org",
 *   "@type": "LodgingReservation",
 *   "reservationId": "RES-1",
 *   "reservationStatus": "https://schema.org/ReservationPending",
 *   "checkinTime": "2027-01-15",
 *   "checkoutTime": "2027-01-18",
 *   "numAdults": 2,
 *   "underName": { "@type": "Person", "identifier": "1", "name": "Ana Torres", "email": "ana@correo.mx" },
 *   "reservationFor": { "@type": "HotelRoom", "identifier": "102", "name": "Habitación 102 · DOBLE" },
 *   "totalPrice": 5400.00,
 *   "priceCurrency": "MXN",
 *   "estadoHotel": "PENDIENTE"
 * }
 *
 * - reservationId "RES-1" es el folio legible para personas; la URI usa
 *   el id numérico: /api/v1/reservas/1.
 * - reservationStatus usa el estado de schema.org; estadoHotel es la
 *   propiedad PROPIA con el estado exacto del hotel (schema.org no
 *   distingue confirmada, en curso y finalizada).
 * - underName y reservationFor son objetos anidados (sin @context: ya
 *   lo hereda del objeto de afuera).
 *
 * Depende de: Reserva, Habitacion, Huesped y EstadoReserva.
 */
public record ReservaRespuesta(
        @JsonProperty("@context") String contexto,
        @JsonProperty("@type") String tipoSchema,
        String reservationId,
        String reservationStatus,
        LocalDate checkinTime,
        LocalDate checkoutTime,
        Integer numAdults,
        Persona underName,
        Cuarto reservationFor,
        BigDecimal totalPrice,
        String priceCurrency,
        String estadoHotel
) {

    /** Prefijo del folio que ven las personas: RES-1, RES-2... */
    public static final String PREFIJO_FOLIO = "RES-";

    /** underName -> Person (versión corta del huésped). */
    public record Persona(
            @JsonProperty("@type") String tipoSchema,
            String identifier,
            String name,
            String email) {
    }

    /** reservationFor -> HotelRoom (versión corta de la habitación). */
    public record Cuarto(
            @JsonProperty("@type") String tipoSchema,
            String identifier,
            String name) {
    }

    /**
     * Convierte la entidad en JSON-LD. Debe llamarse DENTRO de una
     * transacción: la habitación y el huésped son LAZY y se leen aquí.
     */
    public static ReservaRespuesta desde(Reserva r) {
        Huesped h = r.getHuesped();
        Habitacion hab = r.getHabitacion();
        return new ReservaRespuesta(
                "https://schema.org",
                "LodgingReservation",
                PREFIJO_FOLIO + r.getId(),
                r.getEstado().getEstadoSchema(),
                r.getFechaEntrada(),
                r.getFechaSalida(),
                r.getNumHuespedes(),
                new Persona("Person", String.valueOf(h.getId()), h.getNombre(), h.getEmail()),
                new Cuarto("HotelRoom", hab.getNumero(), "Habitación " + hab.getNumero() + " · " + hab.getTipo()),
                r.getTotal(),
                HabitacionRespuesta.MONEDA_BASE,
                r.getEstado().name()
        );
    }
}
