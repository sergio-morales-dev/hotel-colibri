package com.example.api.dto;

import com.example.api.model.TipoHabitacion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * HabitacionPeticion · cuerpo de POST y PUT /api/v1/habitaciones
 * ------------------------------------------------------------------
 * Ejemplo:
 *   { "numero": "301", "tipo": "DOBLE", "capacidad": 2, "precioNoche": 1950.00 }
 *
 * ¿Por qué un DTO y no recibir la entidad Habitacion directo?
 *  - El cliente no debe poder mandar "id" ni "activa": esos los
 *    controla el servidor.
 *  - Las reglas de validación de ENTRADA viven aquí, separadas de la
 *    tabla de la base de datos.
 *
 * Es un "record" (Java 16+): una clase solo de datos, inmutable, con
 * constructor y getters generados (numero(), tipo()...).
 * Las anotaciones se revisan cuando el controller pone @Valid; si una
 * falla, el ManejadorErrores responde 400 con la lista de "errores".
 */
public record HabitacionPeticion(

        @NotBlank(message = "es obligatorio")
        @Size(max = 10, message = "máximo 10 caracteres")
        String numero,

        @NotNull(message = "es obligatorio (SENCILLA, DOBLE o SUITE)")
        TipoHabitacion tipo,

        @NotNull(message = "es obligatoria")
        @Min(value = 1, message = "debe ser al menos 1")
        @Max(value = 10, message = "no puede ser mayor que 10")
        Integer capacidad,

        @NotNull(message = "es obligatorio")
        @DecimalMin(value = "0.01", message = "debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "máximo 8 enteros y 2 decimales")
        BigDecimal precioNoche
) {
}
