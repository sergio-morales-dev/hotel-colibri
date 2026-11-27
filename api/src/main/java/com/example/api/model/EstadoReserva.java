package com.example.api.model;

/**
 * EstadoReserva · ciclo de vida de una reserva (Hotel Colibrí)
 * ------------------------------------------------------------------
 *   Crear              -> PENDIENTE
 *   PENDIENTE  + pagar     -> CONFIRMADA
 *   PENDIENTE  + cancelar  -> CANCELADA
 *   CONFIRMADA + cancelar  -> CANCELADA (con reembolso)
 *   CONFIRMADA + llegada   -> EN_CURSO
 *   EN_CURSO   + salida    -> FINALIZADA
 *   Cualquier otra transición -> 409 Conflict
 * (Hoy solo se usa PENDIENTE; pagar, cancelar, llegada y salida son
 *  de la entrega final del 4 de diciembre.)
 *
 * Cada estado guarda también su equivalente en schema.org
 * (ReservationStatusType), según el glosario del equipo. schema.org no
 * distingue confirmada / en curso / finalizada (las tres son
 * ReservationConfirmed); por eso la respuesta lleva además la
 * propiedad propia "estadoHotel" con el nombre exacto del estado.
 *
 * Una reserva CANCELADA ya no ocupa la habitación: no cuenta para la
 * regla de traslape.
 *
 * Depende de: nada.
 */
public enum EstadoReserva {

    PENDIENTE("https://schema.org/ReservationPending"),
    CONFIRMADA("https://schema.org/ReservationConfirmed"),
    EN_CURSO("https://schema.org/ReservationConfirmed"),
    FINALIZADA("https://schema.org/ReservationConfirmed"),
    CANCELADA("https://schema.org/ReservationCancelled");

    // Un enum puede tener atributos: así el mapeo a schema.org vive junto
    // al estado y no repartido en ifs por todo el código.
    private final String estadoSchema;

    EstadoReserva(String estadoSchema) {
        this.estadoSchema = estadoSchema;
    }

    public String getEstadoSchema() {
        return estadoSchema;
    }
}
