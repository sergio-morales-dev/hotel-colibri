package com.example.api.exception;

import org.springframework.http.HttpStatus;

/**
 * ConflictoException · 409 Conflict
 * La petición es válida, pero choca con el ESTADO ACTUAL de los datos:
 *  - la habitación ya está ocupada en esas fechas (traslape),
 *  - el número de habitación o el correo ya existen,
 *  - transición de estado inválida (ej. pagar una reserva cancelada).
 */
public class ConflictoException extends ApiException {

    public ConflictoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
