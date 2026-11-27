package com.example.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reserva · Hotel Colibrí
 * ------------------------------------------------------------------
 * Entidad JPA: cada objeto es una fila de la tabla "reservas".
 *
 * Relaciones (regla 6.2: "las relaciones JPA son @ManyToOne"):
 *  - Muchas reservas -> UNA habitación  (columna habitacion_id)
 *  - Muchas reservas -> UN huésped      (columna huesped_id)
 *  Se guarda solo la llave foránea; la habitación y el huésped viven
 *  en sus propias tablas (no se repiten sus datos en cada reserva).
 *
 *  fetch = LAZY: al leer una reserva, Hibernate NO trae la habitación
 *  ni el huésped hasta que se piden (getHabitacion()). Ahorra consultas.
 *  Por eso la conversión a JSON se hace dentro del service, con la
 *  transacción abierta.
 *
 * Decisiones (para la defensa):
 *  - Fechas con LocalDate (solo día, sin hora ni zona horaria). Regla 6.2.
 *  - total con BigDecimal y lo CALCULA EL SERVIDOR (noches × precio por
 *    noche) al crear la reserva. El cliente nunca envía el precio.
 *  - El total se GUARDA (no se recalcula al consultar): si mañana sube
 *    el precio de la habitación, las reservas ya hechas conservan el
 *    precio con el que se reservaron, como en un hotel real.
 *  - Toda reserva nace PENDIENTE.
 *
 * Depende de: Habitacion, Huesped y EstadoReserva.
 */
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "huesped_id", nullable = false)
    private Huesped huesped;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "num_huespedes", nullable = false)
    private Integer numHuespedes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    /** Constructor vacío que exige JPA. */
    protected Reserva() {
    }

    /**
     * Crea una reserva nueva en estado PENDIENTE. El total llega ya
     * calculado por ReservaService (no por el cliente).
     */
    public Reserva(Habitacion habitacion, Huesped huesped, LocalDate fechaEntrada,
                   LocalDate fechaSalida, Integer numHuespedes, BigDecimal total) {
        this.habitacion = habitacion;
        this.huesped = huesped;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.numHuespedes = numHuespedes;
        this.total = total;
    }

    // ---------------- Getters ----------------
    // Sin setters: hoy una reserva no se edita. Los cambios de estado
    // (pagar, cancelar...) se agregarán como métodos con nombre propio
    // en la entrega final, para validar cada transición.

    public Long getId() {
        return id;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public Huesped getHuesped() {
        return huesped;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public Integer getNumHuespedes() {
        return numHuespedes;
    }

    /** Total de la reserva en MXN (2 decimales). Lo usa también la cotización (Jaqueline). */
    public BigDecimal getTotal() {
        return total;
    }

    public EstadoReserva getEstado() {
        return estado;
    }
}
