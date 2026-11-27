package com.example.api.exception;

import org.springframework.http.HttpStatus;

/**
 * RecursoNoEncontradoException · 404 Not Found
 * Se lanza cuando se pide algo que no existe: una habitación, un
 * huésped o una reserva con ese número/id.
 * Ejemplo: GET /api/v1/habitaciones/999
 */
public class RecursoNoEncontradoException extends ApiException {

    public RecursoNoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }
}
