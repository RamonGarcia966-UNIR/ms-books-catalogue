package es.unir.dwfs.catalogue.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones para la API de catálogo de libros
 * Anotado con @ControllerAdvice que interceptará las excepciones
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de violación de integridad de datos
     * Detecta el tipo específico de violación analizando el mensaje de error
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {

        log.error("Error de integridad de datos: {}", ex.getMessage(), ex);

        String errorMessage;
        String exceptionMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        // Detectar violación de ISBN duplicado
        if (exceptionMessage.contains("isbn") &&
                (exceptionMessage.contains("unique") || exceptionMessage.contains("unicidad"))) {
            errorMessage = "El ISBN ya existe en el sistema. Por favor, utilice un ISBN diferente.";
        }
        // Detectar violación de clave primaria
        else if (exceptionMessage.contains("primary key") || exceptionMessage.contains("clave primaria")) {
            errorMessage = "Ya existe un registro con el mismo identificador.";
        }
        // Detectar violación de NOT NULL
        else if (exceptionMessage.contains("not null") || exceptionMessage.contains("null")) {
            errorMessage = "Faltan campos obligatorios. Por favor, complete todos los datos requeridos.";
        }
        // Violación genérica de integridad
        else {
            errorMessage = "Error de integridad de datos. Verifique que los datos sean válidos y no estén duplicados.";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(errorMessage)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja excepciones de ResponseStatusException lanzadas explícitamente
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            WebRequest request) {

        log.error("ResponseStatusException: {}", ex.getReason(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatusCode().value())
                .error(HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase())
                .message(ex.getReason() != null ? ex.getReason() : "Error en la petición")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    /**
     * Maneja todas las demás excepciones no controladas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Error inesperado: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ha ocurrido un error inesperado. Por favor, contacte al administrador.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
