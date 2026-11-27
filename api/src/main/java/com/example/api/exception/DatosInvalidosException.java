package com.example.api.exception;

import org.springframework.http.HttpStatus;

/**
 * DatosInvalidosException · 400 Bad Request
 * Para validaciones que NO se pueden expresar con anotaciones como
 * @NotNull, porque comparan dos campos entre sí. Ejemplos:
 *  - la fecha de salida no es posterior a la de entrada,
 *  - la fecha de entrada está en el pasado (regla 6.3).
 * Las validaciones de un solo campo (@NotBlank, @Min...) las responde
 * Spring solo y el ManejadorErrores les da formato.
 */
public class DatosInvalidosException extends ApiException {

    public DatosInvalidosException(String mensaje) {
        super(HttpStatus.BAD_REQUEST, mensaje);
    }
}
