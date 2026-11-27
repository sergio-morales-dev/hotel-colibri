package com.example.api.service;

import com.example.api.dto.ReservaPeticion;
import com.example.api.dto.ReservaRespuesta;
import com.example.api.exception.ConflictoException;
import com.example.api.exception.RecursoNoEncontradoException;
import com.example.api.exception.ReglaNegocioException;
import com.example.api.model.Habitacion;
import com.example.api.model.Huesped;
import com.example.api.model.Reserva;
import com.example.api.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * ReservaService · reglas de negocio de las reservas (Hotel Colibrí)
 * ------------------------------------------------------------------
 * Al CREAR una reserva se revisa, en este orden:
 *   1. Fechas (400): la salida debe ser posterior a la entrada y la
 *      entrada no puede estar en el pasado (regla 6.3).
 *   2. Existencia (404): el huésped y la habitación deben existir.
 *   3. Reglas de la habitación (422): debe estar activa y el número de
 *      huéspedes no puede superar su capacidad (regla 6.5).
 *   4. Estado actual (409): no puede haber traslape con otra reserva
 *      activa de esa habitación (regla 6.4).
 *   5. Total = noches × precio por noche, lo calcula el servidor.
 *
 * ¿Por qué ese orden? De lo más barato y básico a lo más costoso:
 * primero lo que se revisa sin tocar la BD, luego que las cosas
 * existan, y al final la consulta de traslape. Además así el cliente
 * recibe el error más útil: si pide 4 personas en una DOBLE, le
 * decimos "capacidad" (422) aunque las fechas también estén ocupadas.
 *
 * Depende de: ReservaRepository, HabitacionService, HuespedService,
 * DTOs de reserva y excepciones.
 */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionService habitacionService;
    private final HuespedService huespedService;

    public ReservaService(ReservaRepository reservaRepository,
                          HabitacionService habitacionService,
                          HuespedService huespedService) {
        this.reservaRepository = reservaRepository;
        this.habitacionService = habitacionService;
        this.huespedService = huespedService;
    }

    @Transactional
    public ReservaRespuesta crear(ReservaPeticion peticion) {
        // 1. Fechas -> 400 (regla compartida con la búsqueda de disponibles)
        ValidadorFechas.validarEstancia(peticion.fechaEntrada(), peticion.fechaSalida());

        // 2. Existencia -> 404 (la habitación se bloquea hasta terminar,
        //    para que dos reservas simultáneas no se encimen)
        Huesped huesped = huespedService.buscarPorId(peticion.huesped());
        Habitacion habitacion = habitacionService.buscarParaReservar(peticion.habitacion());

        // 3. Reglas de la habitación -> 422
        if (!habitacion.isActiva()) {
            throw new ReglaNegocioException("La habitación " + habitacion.getNumero()
                    + " está inactiva y no se puede reservar");
        }
        if (peticion.numHuespedes() > habitacion.getCapacidad()) {
            throw new ReglaNegocioException("La habitación " + habitacion.getNumero() + " ("
                    + habitacion.getTipo() + ") admite máximo " + habitacion.getCapacidad()
                    + " huésped(es); se pidieron " + peticion.numHuespedes());
        }

        // 4. Traslape -> 409
        if (reservaRepository.existeTraslape(habitacion, peticion.fechaEntrada(), peticion.fechaSalida())) {
            throw new ConflictoException("La habitación " + habitacion.getNumero()
                    + " ya está reservada en esas fechas");
        }

        // 5. Total calculado por el servidor
        BigDecimal total = calcularTotal(habitacion.getPrecioNoche(),
                peticion.fechaEntrada(), peticion.fechaSalida());

        Reserva nueva = new Reserva(habitacion, huesped, peticion.fechaEntrada(),
                peticion.fechaSalida(), peticion.numHuespedes(), total);
        return ReservaRespuesta.desde(reservaRepository.save(nueva)); // nace PENDIENTE
    }

    @Transactional(readOnly = true)
    public ReservaRespuesta obtener(Long id) {
        // Se convierte aquí, con la transacción abierta, porque la
        // habitación y el huésped de la reserva son LAZY.
        return ReservaRespuesta.desde(buscarPorId(id));
    }

    /**
     * Busca la ENTIDAD o lanza 404.
     * Acuerdo con Jaqueline: la cotización la usa así →
     *   reservaService.buscarPorId(id).getTotal()
     */
    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la reserva " + id));
    }

    // ---------------- Regla auxiliar (privada) ----------------

    /**
     * Total = noches × precio por noche.
     * Noches = días entre entrada y salida (15 -> 18 de enero = 3 noches).
     * Ejemplo prueba 2: 3 × 1,800.00 = 5,400.00 MXN.
     * setScale(2, HALF_UP): siempre 2 decimales, redondeo comercial.
     */
    private BigDecimal calcularTotal(BigDecimal precioNoche, LocalDate entrada, LocalDate salida) {
        long noches = ChronoUnit.DAYS.between(entrada, salida);
        return precioNoche.multiply(BigDecimal.valueOf(noches)).setScale(2, RoundingMode.HALF_UP);
    }
}
