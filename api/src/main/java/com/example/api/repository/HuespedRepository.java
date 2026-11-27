package com.example.api.repository;

import com.example.api.model.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * HuespedRepository · acceso a la tabla "huespedes"
 * ------------------------------------------------------------------
 * findById, save, etc. vienen de JpaRepository.
 * existsByEmail se arma solo a partir del nombre del método:
 *   SELECT COUNT(*) > 0 FROM huespedes WHERE email = ?
 * Se usa para responder 409 si el correo ya está registrado.
 *
 * Depende de: Huesped.
 */
public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    boolean existsByEmail(String email);
}
