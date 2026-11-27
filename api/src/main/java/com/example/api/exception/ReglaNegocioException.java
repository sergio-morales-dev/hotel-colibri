package com.example.api.exception;

import org.springframework.http.HttpStatus;

/**
 * ReglaNegocioException · 422 Unprocessable Content
 * Los datos están bien escritos, pero una regla del hotel impide la
 * operación:
 *  - más huéspedes que la capacidad de la habitación,
 *  - la habitación está inactiva.
 *
 * Diferencia con 400: en 400 el dato está MAL (fecha imposible, campo
 * vacío); en 422 el dato es correcto pero el negocio no lo permite.
 *
 * Se usa HttpStatus.valueOf(422) porque en Spring 7 el nombre de la
 * constante cambió (UNPROCESSABLE_ENTITY -> UNPROCESSABLE_CONTENT);
 * con el número evitamos depender de cualquiera de los dos nombres.
 */
public class ReglaNegocioException extends ApiException {

    public ReglaNegocioException(String mensaje) {
        super(HttpStatus.valueOf(422), mensaje);
    }
}
