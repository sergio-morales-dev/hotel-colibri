package com.example.api.config;

import com.example.api.model.Habitacion;
import com.example.api.model.TipoHabitacion;
import com.example.api.repository.HabitacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * CargaInicial · Hotel Colibrí
 * ------------------------------------------------------------------
 * Al arrancar la API, registra las 3 habitaciones del enunciado:
 *   101 SENCILLA cap. 1 $1,200.00 · 102 DOBLE cap. 2 $1,800.00
 *   201 SUITE    cap. 4 $3,500.00
 *
 * CommandLineRunner: Spring ejecuta run() una vez, cuando la API ya
 * terminó de arrancar (con la BD conectada y las tablas creadas).
 *
 * ¿Por qué "solo si la tabla está vacía"? Porque ddl-auto=update NO
 * borra los datos al reiniciar: sin esa revisión, cada arranque
 * intentaría insertar las mismas habitaciones y fallaría por el número
 * repetido.
 *
 * ¿Por qué no un data.sql? Así queda en Java, con los mismos tipos
 * (enum, BigDecimal) y la misma validación que el resto del código.
 *
 * Depende de: HabitacionRepository, Habitacion y TipoHabitacion.
 */
@Component
public class CargaInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    private final HabitacionRepository habitacionRepository;

    public CargaInicial(HabitacionRepository habitacionRepository) {
        this.habitacionRepository = habitacionRepository;
    }

    @Override
    public void run(String... args) {
        if (habitacionRepository.count() > 0) {
            log.info("Carga inicial omitida: ya hay habitaciones registradas");
            return;
        }
        // Los precios van como texto en BigDecimal para guardar el valor exacto
        habitacionRepository.saveAll(List.of(
                new Habitacion("101", TipoHabitacion.SENCILLA, 1, new BigDecimal("1200.00")),
                new Habitacion("102", TipoHabitacion.DOBLE, 2, new BigDecimal("1800.00")),
                new Habitacion("201", TipoHabitacion.SUITE, 4, new BigDecimal("3500.00"))
        ));
        log.info("Carga inicial: se registraron las habitaciones 101, 102 y 201");
    }
}
