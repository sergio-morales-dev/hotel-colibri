package com.example.api.exception;

import org.springframework.http.HttpStatus;

/**
 * ApiException · Hotel Colibrí
 * ------------------------------------------------------------------
 * Clase BASE de los errores de negocio de la API. Cada error "sabe"
 * qué código HTTP le corresponde (404, 409, 422, 400...).
 *
 * ¿Por qué una clase base?
 *  - El ManejadorErrores atiende a TODAS con un solo método, en vez de
 *    repetir un método casi igual por cada excepción.
 *  - Para agregar un error nuevo basta con crear una subclase de
 *    3 líneas; el manejador no se toca.
 *
 * Es "abstract": nadie lanza una ApiException genérica, siempre una
 * de sus hijas, que dicen QUÉ pasó (no existe, conflicto, etc.).
 *
 * Extiende RuntimeException (excepción "no verificada"): así no hay
 * que escribir "throws" en cada método del service.
 *
 * Depende de: nada (solo Spring).
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus estado;

    protected ApiException(HttpStatus estado, String mensaje) {
        super(mensaje); // el mensaje se envía al cliente en el campo "detail"
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
