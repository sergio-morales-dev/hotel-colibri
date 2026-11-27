package com.example.api.dto;

import com.example.api.model.Huesped;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HuespedRespuesta · lo que la API devuelve de un huésped
 * ------------------------------------------------------------------
 * JSON-LD con schema.org, tipo Person (glosario del equipo):
 * {
 *   "@context": "https://schema.org",
 *   "@type": "Person",
 *   "identifier": "1",
 *   "name": "Ana Torres",
 *   "email": "ana@correo.mx",
 *   "telephone": "2291234567"
 * }
 * identifier es el id que se usa en la URI (/api/v1/huespedes/1) y en
 * el campo "huesped" al crear una reserva.
 *
 * @JsonInclude(NON_NULL): si el huésped no dio teléfono, el campo
 * "telephone" no aparece (en vez de salir "telephone": null).
 *
 * Depende de: Huesped.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HuespedRespuesta(
        @JsonProperty("@context") String contexto,
        @JsonProperty("@type") String tipoSchema,
        String identifier,
        String name,
        String email,
        String telephone
) {

    public static HuespedRespuesta desde(Huesped h) {
        return new HuespedRespuesta(
                "https://schema.org",
                "Person",
                String.valueOf(h.getId()),
                h.getNombre(),
                h.getEmail(),
                h.getTelefono()
        );
    }
}
