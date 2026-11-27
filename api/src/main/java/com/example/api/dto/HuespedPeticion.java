package com.example.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * HuespedPeticion · cuerpo de POST /api/v1/huespedes
 * ------------------------------------------------------------------
 * Ejemplo:
 *   { "nombre": "Ana Torres", "email": "ana@correo.mx", "telefono": "2291234567" }
 *
 * - telefono es opcional: @Pattern deja pasar null (si no se manda),
 *   pero si se manda debe tener de 7 a 20 caracteres entre dígitos,
 *   espacios, +, - y paréntesis.
 * - @Email revisa que el correo tenga forma de correo (algo@algo).
 */
public record HuespedPeticion(

        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "es obligatorio")
        @Email(message = "no tiene formato de correo")
        @Size(max = 120, message = "máximo 120 caracteres")
        String email,

        @Pattern(regexp = "^[0-9+()\\- ]{7,20}$",
                message = "debe tener de 7 a 20 dígitos (se permiten espacios, +, - y paréntesis)")
        String telefono
) {
}
