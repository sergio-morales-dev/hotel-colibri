package com.example.api.repository;

import com.example.api.model.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;

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
 * La consulta de habitaciones DISPONIBLES (sin traslape de fechas)
 * se agrega en la fase 3, cuando ya exista la entidad Reserva.
 *
 * Depende de: Habitacion.
 */
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    // Optional obliga a quien lo llame a decidir qué hacer si no existe
    // (en el service lo convertiremos en un 404), en vez de recibir null.
    Optional<Habitacion> findByNumero(String numero);

    // Para responder 409 si alguien intenta dar de alta un número repetido.
    boolean existsByNumero(String numero);
}
