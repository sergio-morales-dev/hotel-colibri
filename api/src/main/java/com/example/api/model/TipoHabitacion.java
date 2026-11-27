package com.example.api.model;

/**
 * TipoHabitacion · Hotel Colibrí
 * ------------------------------------------------------------------
 * Los únicos tipos de habitación que maneja el hotel (regla 6.2 del
 * proyecto: "el tipo de habitación es un enum").
 *
 * ¿Por qué un enum y no un String?
 *  - Con String se podría guardar "Doble", "doble" o "DOBEL" y el
 *    sistema no se daría cuenta. Con enum, Java solo acepta estos 3.
 *  - Si en el JSON llega un tipo que no existe (ej. "PENTHOUSE"),
 *    Spring rechaza la petición antes de llegar a nuestro código y
 *    responde 400.
 *
 * Depende de: nada (por eso es el primer archivo de la fase).
 */
public enum TipoHabitacion {
    SENCILLA,
    DOBLE,
    SUITE
}
