package com.example.api.dto;

import com.example.api.model.Habitacion;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * HabitacionRespuesta · lo que la API devuelve de una habitación
 * ------------------------------------------------------------------
 * Formato JSON-LD con el vocabulario de schema.org (tipo HotelRoom),
 * para que cualquier sistema (buscador, agencia de viajes) entienda
 * los datos sin un acuerdo previo con nosotros:
 *
 * {
 *   "@context": "https://schema.org",
 *   "@type": "HotelRoom",
 *   "identifier": "102",
 *   "name": "Habitación 102 · DOBLE",
 *   "accommodationCategory": "DOBLE",
 *   "occupancy": { "@type": "QuantitativeValue", "maxValue": 2 },
 *   "offers": { "@type": "Offer", "price": 1800.00, "priceCurrency": "MXN" },
 *   "activa": true
 * }
 *
 * - "@context" y "@type" empiezan con @, que no es válido en un nombre
 *   de variable Java; por eso @JsonProperty les pone el nombre en el JSON.
 * - accommodationCategory: propiedad de schema.org para la categoría
 *   de un alojamiento (aquí: el tipo SENCILLA/DOBLE/SUITE).
 * - activa: propiedad PROPIA del hotel (schema.org no tiene una para
 *   "habitación dada de baja"), igual que estadoHotel en la reserva.
 *
 * Depende de: Habitacion.
 */
public record HabitacionRespuesta(
        @JsonProperty("@context") String contexto,
        @JsonProperty("@type") String tipoSchema,
        String identifier,
        String name,
        String accommodationCategory,
        Ocupacion occupancy,
        Oferta offers,
        boolean activa
) {

    /** Moneda base del hotel: todos los precios se guardan en pesos. */
    public static final String MONEDA_BASE = "MXN";

    /** occupancy -> QuantitativeValue: cuántas personas caben como máximo. */
    public record Ocupacion(
            @JsonProperty("@type") String tipoSchema,
            Integer maxValue) {
    }

    /** offers -> Offer: el precio por noche y su moneda. */
    public record Oferta(
            @JsonProperty("@type") String tipoSchema,
            BigDecimal price,
            String priceCurrency) {
    }

    /**
     * Convierte la entidad (tabla) en la respuesta (JSON-LD).
     * Está aquí para que el controller y el service no repitan esta
     * conversión cada vez que devuelven una habitación.
     */
    public static HabitacionRespuesta desde(Habitacion h) {
        return new HabitacionRespuesta(
                "https://schema.org",
                "HotelRoom",
                h.getNumero(),
                "Habitación " + h.getNumero() + " · " + h.getTipo(),
                h.getTipo().name(),
                new Ocupacion("QuantitativeValue", h.getCapacidad()),
                new Oferta("Offer", h.getPrecioNoche(), MONEDA_BASE),
                h.isActiva()
        );
    }
}
