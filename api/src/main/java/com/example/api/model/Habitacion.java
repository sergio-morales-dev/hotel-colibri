package com.example.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Habitacion · Hotel Colibrí
 * ------------------------------------------------------------------
 * Entidad JPA: cada objeto Habitacion es una fila de la tabla
 * "habitaciones" en MySQL. Hibernate crea la tabla sola gracias a
 * spring.jpa.hibernate.ddl-auto=update.
 *
 * Decisiones (para la defensa):
 *  - id (Long autonumérico) es la llave interna de la BD; "numero"
 *    ("101", "102"...) es como el hotel identifica la habitación y es
 *    lo que se usa en las URIs: /api/v1/habitaciones/102.
 *    Se separan porque el número es un dato del negocio (podría
 *    cambiar), y la llave de la BD no debe cambiar nunca.
 *  - numero es String y no int: es un identificador, no se suma ni
 *    se resta (igual que un teléfono), y así admite "PH1" si un día
 *    existe. Además schema.org lo pide como texto (identifier).
 *  - precioNoche es BigDecimal: el dinero nunca va en double porque
 *    double no guarda exacto valores como 0.1 (regla 6.2).
 *  - activa: las habitaciones con reservas NO se borran, se
 *    desactivan (regla 6.5). Una inactiva no se puede reservar.
 *
 * Depende de: TipoHabitacion.
 */
@Entity
@Table(name = "habitaciones")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL genera el id (AUTO_INCREMENT)
    private Long id;

    // unique = true: MySQL no permite dos habitaciones con el mismo número
    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    // EnumType.STRING guarda el texto "DOBLE" en la tabla.
    // (Con ORDINAL guardaría 0, 1, 2 y se rompería si se reordena el enum.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoHabitacion tipo;

    @Column(nullable = false)
    private Integer capacidad;

    // precision 10, scale 2 -> hasta 99,999,999.99 con exactamente 2 decimales
    @Column(name = "precio_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioNoche;

    @Column(nullable = false)
    private boolean activa = true; // toda habitación nueva nace activa

    /** JPA necesita un constructor vacío para crear los objetos al leer de la BD. */
    protected Habitacion() {
    }

    /** Constructor para crear habitaciones nuevas (lo usa la carga inicial y el service). */
    public Habitacion(String numero, TipoHabitacion tipo, Integer capacidad, BigDecimal precioNoche) {
        this.numero = numero;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioNoche = precioNoche;
    }

    // ---------------- Getters y setters ----------------
    // No hay setId: el id lo asigna MySQL, nadie debe cambiarlo.

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public TipoHabitacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoHabitacion tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public BigDecimal getPrecioNoche() {
        return precioNoche;
    }

    public void setPrecioNoche(BigDecimal precioNoche) {
        this.precioNoche = precioNoche;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
