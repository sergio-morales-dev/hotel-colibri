package com.example.api.service;

import com.example.api.dto.HabitacionPeticion;
import com.example.api.dto.HabitacionRespuesta;
import com.example.api.exception.ConflictoException;
import com.example.api.exception.DatosInvalidosException;
import com.example.api.exception.RecursoNoEncontradoException;
import com.example.api.model.Habitacion;
import com.example.api.repository.HabitacionRepository;
import com.example.api.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * HabitacionService · Hotel Colibrí
 * ------------------------------------------------------------------
 * Reglas de negocio de las habitaciones. El controller solo recibe la
 * petición y llama aquí; aquí se decide si se puede o no, y se lanza
 * la excepción adecuada (que el ManejadorErrores vuelve ProblemDetail).
 *
 * Reglas que aplica:
 *  - No puede haber dos habitaciones con el mismo número -> 409.
 *  - Si el número no existe -> 404.
 *  - El número es el identificador del recurso (va en la URI), por eso
 *    un PUT no lo puede cambiar -> 400.
 *  - Las habitaciones no se borran: se activan/desactivan (regla 6.5).
 *
 * @Transactional: cada método público corre en una transacción; si algo
 * falla a la mitad, MySQL deshace los cambios. readOnly = true en las
 * consultas le avisa a Hibernate que no habrá cambios (más ligero).
 *
 * Depende de: HabitacionRepository, DTOs de habitación y excepciones.
 */
@Service
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final ReservaRepository reservaRepository; // solo para la búsqueda de disponibles

    // Inyección por constructor: Spring pasa los repositories al crear el
    // service. Es la forma recomendada (los campos pueden ser final y es
    // fácil de probar).
    public HabitacionService(HabitacionRepository habitacionRepository,
                             ReservaRepository reservaRepository) {
        this.habitacionRepository = habitacionRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<HabitacionRespuesta> listar() {
        return habitacionRepository.findAll().stream()
                .map(HabitacionRespuesta::desde) // entidad -> JSON-LD
                .toList();
    }

    /**
     * Habitaciones DISPONIBLES para una estancia (prueba 14):
     *   1. Fechas válidas (misma regla 6.3 que al reservar) -> si no, 400.
     *   2. Candidatas: activas y con capacidad >= huéspedes.
     *   3. Se quitan las que tienen traslape con una reserva activa.
     *
     * Se REUTILIZA ReservaRepository.existeTraslape: la regla de traslape
     * existe una sola vez en todo el proyecto, así que "disponible" aquí
     * significa exactamente "se podría reservar" en POST /reservas.
     * (Hace una consulta por habitación candidata; con las pocas
     * habitaciones de un hotel es despreciable.)
     */
    @Transactional(readOnly = true)
    public List<HabitacionRespuesta> disponibles(LocalDate entrada, LocalDate salida, int huespedes) {
        ValidadorFechas.validarEstancia(entrada, salida);
        return habitacionRepository.findByActivaTrueAndCapacidadGreaterThanEqualOrderByNumeroAsc(huespedes)
                .stream()
                .filter(h -> !reservaRepository.existeTraslape(h, entrada, salida))
                .map(HabitacionRespuesta::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitacionRespuesta obtener(String numero) {
        return HabitacionRespuesta.desde(buscarPorNumero(numero));
    }

    @Transactional
    public HabitacionRespuesta crear(HabitacionPeticion peticion) {
        if (habitacionRepository.existsByNumero(peticion.numero())) {
            throw new ConflictoException("Ya existe la habitación " + peticion.numero());
        }
        Habitacion nueva = new Habitacion(peticion.numero(), peticion.tipo(),
                peticion.capacidad(), peticion.precioNoche());
        return HabitacionRespuesta.desde(habitacionRepository.save(nueva));
    }

    @Transactional
    public HabitacionRespuesta actualizar(String numero, HabitacionPeticion peticion) {
        if (!numero.equals(peticion.numero())) {
            throw new DatosInvalidosException("El número del cuerpo (" + peticion.numero()
                    + ") debe ser igual al de la URI (" + numero + "): el número no se puede cambiar");
        }
        Habitacion habitacion = buscarPorNumero(numero);
        habitacion.setTipo(peticion.tipo());
        habitacion.setCapacidad(peticion.capacidad());
        habitacion.setPrecioNoche(peticion.precioNoche());
        // No hace falta llamar a save(): dentro de @Transactional, Hibernate
        // detecta los cambios del objeto y hace el UPDATE al terminar.
        return HabitacionRespuesta.desde(habitacion);
    }

    @Transactional
    public HabitacionRespuesta cambiarEstado(String numero, boolean activa) {
        Habitacion habitacion = buscarPorNumero(numero);
        habitacion.setActiva(activa);
        return HabitacionRespuesta.desde(habitacion);
    }

    /**
     * Busca la ENTIDAD o lanza 404. Es pública porque ReservaService
     * (fase 3) también la necesita: así la regla "si no existe -> 404"
     * se escribe una sola vez.
     */
    public Habitacion buscarPorNumero(String numero) {
        return habitacionRepository.findByNumero(numero)
                .orElseThrow(() -> noExiste(numero));
    }

    /**
     * Igual que buscarPorNumero, pero bloquea la habitación mientras dura
     * la transacción de quien la llama (ver HabitacionRepository).
     * La usa ReservaService para que dos reservas simultáneas de la misma
     * habitación no se encimen.
     */
    public Habitacion buscarParaReservar(String numero) {
        return habitacionRepository.buscarParaReservar(numero)
                .orElseThrow(() -> noExiste(numero));
    }

    // El mensaje del 404 se arma en un solo lugar (no se repite el texto).
    private RecursoNoEncontradoException noExiste(String numero) {
        return new RecursoNoEncontradoException("No existe la habitación " + numero);
    }
}
