package com.example.api.repository;

import com.example.api.model.Habitacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * HabitacionRepository · Hotel Colibrí
 * ------------------------------------------------------------------
 * Acceso a la tabla "habitaciones". Es una INTERFAZ: Spring Data JPA
 * escribe la implementación sola al arrancar.
 *
 * Al extender JpaRepository<Habitacion, Long> ya tenemos gratis:
 * save, findAll, findById, count, delete...  (Long = tipo del id).
 *
 * Los métodos de abajo son "consultas derivadas": Spring lee el
 * NOMBRE del método y arma el SQL. Por ejemplo, findByNumero se
 * convierte en  SELECT * FROM habitaciones WHERE numero = ?
 *
 * La búsqueda de DISPONIBLES usa findByActivaTrue... (candidatas) y
 * luego ReservaRepository.existeTraslape (ver HabitacionService).
 *
 * Depende de: Habitacion.
 */
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    // Optional obliga a quien lo llame a decidir qué hacer si no existe
    // (en el service lo convertiremos en un 404), en vez de recibir null.
    Optional<Habitacion> findByNumero(String numero);

    // Para responder 409 si alguien intenta dar de alta un número repetido.
    boolean existsByNumero(String numero);

    // Candidatas para la búsqueda de disponibles: activas y con cupo.
    // El nombre se traduce a:
    //   WHERE activa = true AND capacidad >= ? ORDER BY numero ASC
    List<Habitacion> findByActivaTrueAndCapacidadGreaterThanEqualOrderByNumeroAsc(Integer huespedes);

    /**
     * Igual que findByNumero, pero BLOQUEA la fila de la habitación hasta
     * que termine la transacción (SELECT ... FOR UPDATE en MySQL).
     *
     * ¿Para qué? Si dos personas reservan la misma habitación en el mismo
     * instante, las dos podrían revisar "¿hay traslape?" antes de que la
     * otra guarde, y las dos pasarían. Con el bloqueo, la segunda espera
     * a que la primera termine y entonces SÍ ve su reserva -> 409.
     * La usa ReservaService al crear una reserva.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Habitacion h WHERE h.numero = :numero")
    Optional<Habitacion> buscarParaReservar(@Param("numero") String numero);
}
