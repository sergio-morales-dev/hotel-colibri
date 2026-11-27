package com.example.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Huesped · Hotel Colibrí
 * ------------------------------------------------------------------
 * Entidad JPA: cada objeto es una fila de la tabla "huespedes".
 * Es la persona a cuyo nombre se hace la reserva (en schema.org,
 * el "underName" de una LodgingReservation es una Person).
 *
 * Decisión del equipo (opción A): el huésped es una entidad propia y
 * la reserva lo enlaza con @ManyToOne. Así un huésped puede tener
 * varias reservas sin repetir su nombre y correo en cada una, y se
 * puede filtrar "las reservas de este huésped".
 *
 * - email es único: identifica a la persona y evita registrarla dos
 *   veces (-> 409). Se guarda en minúsculas para que "Ana@Correo.mx"
 *   y "ana@correo.mx" cuenten como el mismo.
 * - telefono es opcional (nullable).
 *
 * Depende de: nada.
 */
@Entity
@Table(name = "huespedes")
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 20)
    private String telefono;

    /** Constructor vacío que exige JPA para leer de la BD. */
    protected Huesped() {
    }

    public Huesped(String nombre, String email, String telefono) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    // ---------------- Getters ----------------
    // Solo getters: por ahora el huésped no se edita (el PUT queda
    // para la entrega final si da tiempo).

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }
}
