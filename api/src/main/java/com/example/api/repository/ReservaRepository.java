package com.example.api.repository;

import com.example.api.model.Habitacion;
import com.example.api.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

/**
 * ReservaRepository · acceso a la tabla "reservas"
 * ------------------------------------------------------------------
 * REGLA DE NO TRASLAPE (regla 6.4):
 * una habitación no puede tener dos reservas ACTIVAS (no canceladas)
 * con fechas que se crucen. La salida de una SÍ puede coincidir con la
 * entrada de otra (el huésped sale en la mañana y el otro entra en la
 * tarde).
 *
 * Dos rangos de fechas [entradaA, salidaA) y [entradaB, salidaB) se
 * cruzan si y solo si:
 *        entradaA < salidaB   Y   salidaA > entradaB
 *
 * Ejemplo con la reserva existente 15 -> 18 de enero:
 *   nueva 17 -> 19:  15 < 19 y 18 > 17  -> se cruzan  -> 409
 *   nueva 18 -> 20:  15 < 20 y 18 > 18? NO           -> sí se permite
 *   nueva 10 -> 15:  15 < 15? NO                     -> sí se permite
 * Por eso se usa < y > ESTRICTOS (con <= y >= se prohibiría que la
 * salida de una coincida con la entrada de otra).
 *
 * @Query: consulta escrita a mano en JPQL (se escribe sobre las CLASES y
 * sus atributos, no sobre las tablas). Para el enum se usa su nombre
 * completo (com.example.api.model.EstadoReserva.CANCELADA).
 *
 * Depende de: Reserva y Habitacion.
 */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Reserva r
            WHERE r.habitacion = :habitacion
              AND r.estado <> com.example.api.model.EstadoReserva.CANCELADA
              AND r.fechaEntrada < :salida
              AND r.fechaSalida > :entrada
            """)
    boolean existeTraslape(@Param("habitacion") Habitacion habitacion,
                           @Param("entrada") LocalDate entrada,
                           @Param("salida") LocalDate salida);
}
