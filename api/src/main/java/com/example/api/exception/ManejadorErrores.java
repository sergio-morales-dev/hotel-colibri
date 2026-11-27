package com.example.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ManejadorErrores · Hotel Colibrí
 * ------------------------------------------------------------------
 * Punto ÚNICO donde las excepciones se convierten en respuestas HTTP
 * con formato ProblemDetail (RFC 9457):
 *   { "type": "about:blank", "title": "Not Found", "status": 404,
 *     "detail": "No existe la habitación 999",
 *     "instance": "/api/v1/habitaciones/999" }
 *
 * ¿Por qué aquí y no un try/catch en cada controller?
 *  - Los controllers quedan limpios: solo reciben y devuelven datos.
 *  - Todos los errores salen con el MISMO formato.
 *  - Ninguna traza interna (stack trace) llega al cliente (regla 6.6).
 *
 * @RestControllerAdvice: Spring aplica esta clase a TODOS los controllers.
 * Extiende ResponseEntityExceptionHandler: así los errores propios de
 * Spring (JSON mal escrito, parámetro con tipo incorrecto, URL que no
 * existe...) también salen como ProblemDetail.
 *
 * Depende de: ApiException y sus subclases.
 */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    /**
     * Errores de NUESTRO negocio (404, 409, 422, 400).
     * Un solo método para todos gracias a la clase base ApiException:
     * cada excepción ya trae su código HTTP.
     */
    @ExceptionHandler(ApiException.class)
    public ProblemDetail manejarErrorDeNegocio(ApiException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getEstado(), ex.getMessage());
    }

    /**
     * 400 por validaciones de anotaciones (@NotBlank, @Min, @NotNull...)
     * en el cuerpo de la petición. Se agrega la propiedad "errores" con
     * el campo y su mensaje, para que el cliente sepa QUÉ corregir:
     *   "errores": { "capacidad": "debe ser al menos 1" }
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Map<String, String> errores = new LinkedHashMap<>(); // conserva el orden de los campos
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problema = ex.getBody(); // ProblemDetail que Spring ya armó (status 400)
        problema.setDetail("Hay datos inválidos en la petición");
        problema.setProperty("errores", errores);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    /**
     * 400 cuando el JSON no se puede leer: llaves mal cerradas, fecha que
     * no es AAAA-MM-DD, tipo de habitación que no existe ("PENTHOUSE")...
     * Spring respondería "Failed to read request" (en inglés y poco útil).
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido. Revise el formato: fechas AAAA-MM-DD, "
                        + "tipo SENCILLA, DOBLE o SUITE y números sin comillas.");
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    /**
     * 500 para cualquier error que NO esperábamos (un bug).
     * La traza se escribe en la consola del SERVIDOR (para depurar),
     * pero al cliente solo le llega un mensaje genérico, sin detalles
     * internos que puedan revelar cómo está hecho el sistema.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado en la API", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Intente más tarde.");
    }
}
