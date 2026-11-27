package com.example.api.service;

import com.example.api.dto.HuespedPeticion;
import com.example.api.dto.HuespedRespuesta;
import com.example.api.exception.ConflictoException;
import com.example.api.exception.RecursoNoEncontradoException;
import com.example.api.model.Huesped;
import com.example.api.repository.HuespedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * HuespedService · reglas de negocio de los huéspedes
 * ------------------------------------------------------------------
 *  - El correo se guarda en minúsculas y sin espacios en los extremos,
 *    para que "Ana@Correo.mx " y "ana@correo.mx" sean el mismo.
 *  - Correo ya registrado -> 409.
 *  - Id que no existe -> 404.
 *
 * Depende de: HuespedRepository, DTOs de huésped y excepciones.
 */
@Service
public class HuespedService {

    private final HuespedRepository huespedRepository;

    public HuespedService(HuespedRepository huespedRepository) {
        this.huespedRepository = huespedRepository;
    }

    @Transactional
    public HuespedRespuesta crear(HuespedPeticion peticion) {
        String email = peticion.email().trim().toLowerCase(Locale.ROOT);
        if (huespedRepository.existsByEmail(email)) {
            throw new ConflictoException("Ya existe un huésped con el correo " + email);
        }
        Huesped nuevo = new Huesped(peticion.nombre().trim(), email, peticion.telefono());
        return HuespedRespuesta.desde(huespedRepository.save(nuevo));
    }

    @Transactional(readOnly = true)
    public HuespedRespuesta obtener(Long id) {
        return HuespedRespuesta.desde(buscarPorId(id));
    }

    /**
     * Busca la ENTIDAD o lanza 404. Pública porque ReservaService la usa
     * al crear una reserva: la regla "si no existe -> 404" vive en un
     * solo lugar.
     */
    public Huesped buscarPorId(Long id) {
        return huespedRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el huésped " + id));
    }
}
