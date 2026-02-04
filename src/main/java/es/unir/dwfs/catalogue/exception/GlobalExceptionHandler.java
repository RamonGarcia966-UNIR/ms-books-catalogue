package es.unir.dwfs.catalogue.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manejador global de excepciones para la API de catálogo de libros
 */
@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ConverterErrors converterErrors;

    /**
     * Maneja errores de validación de Bean Validation en request bodies (@Valid)
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.error("Error de validación: {}", ex.getMessage());

        List<ErrorResponse.ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.ErrorDetail.builder()
                        .element(error.getField())
                        .code(error.getDefaultMessage())
                        .description(converterErrors.getMessage(error.getDefaultMessage()))
                        .build())
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Error de validación")
                .path(request.getDescription(false).replace("uri=", ""))
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de validación de Bean Validation en parámetros (@Validated)
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        log.error("Error de validación de constraint: {}", ex.getMessage());

        List<ErrorResponse.ErrorDetail> details = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            ErrorResponse.ErrorDetail detail = ErrorResponse.ErrorDetail.builder()
                    .element(violation.getPropertyPath().toString())
                    .code(violation.getMessage())
                    .description(converterErrors.getMessage(violation.getMessage()))
                    .build();
            details.add(detail);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Error de validación")
                .path(request.getDescription(false).replace("uri=", ""))
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de JSON mal formado o tipos incorrectos
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        log.error("Error de formato JSON: {}", ex.getMessage());

        String errorMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        List<ErrorResponse.ErrorDetail> details = new ArrayList<>();

        // Detectar si es un error de fecha
        if (errorMessage.contains("localdate") || errorMessage.contains("publicationdate")) {
            details.add(ErrorResponse.ErrorDetail.builder()
                    .element("publicationDate")
                    .code("BOOK-071")
                    .description(converterErrors.getMessage("BOOK-071"))
                    .build());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("El formato de la petición es incorrecto")
                .path(request.getDescription(false).replace("uri=", ""))
                .details(details.isEmpty() ? null : details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de tipo incorrecto en parámetros de query o path
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {

        log.error("Error de tipo de argumento: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("El parámetro '" + ex.getName() + "' tiene un tipo incorrecto")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja violaciones de reglas de negocio
     * HTTP 422 - Unprocessable Entity
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            BusinessRuleViolationException ex,
            WebRequest request) {

        log.error("Error de regla de negocio: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.valueOf(422).value())
                .error(HttpStatus.valueOf(422).getReasonPhrase())
                .message("Error de validación de reglas de negocio")
                .path(request.getDescription(false).replace("uri=", ""))
                .details(ex.getErrors())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(422));
    }

    /**
     * Maneja excepciones de violación de integridad de datos
     * HTTP 409 - Conflict
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {

        log.error("Error de integridad de datos: {}", ex.getMessage(), ex);

        String errorMessage;
        List<ErrorResponse.ErrorDetail> details = new ArrayList<>();
        String exceptionMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        // Detectar violación de ISBN duplicado
        if (exceptionMessage.contains("isbn") &&
                (exceptionMessage.contains("unique") || exceptionMessage.contains("unicidad"))) {
            errorMessage = "El ISBN ya existe en el sistema";
            details.add(ErrorResponse.ErrorDetail.builder()
                    .element("isbn")
                    .code("BOOK-022")
                    .description(converterErrors.getMessage("BOOK-022"))
                    .build());
        }
        // Detectar violación de clave primaria
        else if (exceptionMessage.contains("primary key") || exceptionMessage.contains("clave primaria")) {
            errorMessage = converterErrors.getMessage("GENERIC-001");
        }
        // Detectar violación de NOT NULL
        else if (exceptionMessage.contains("not null") || exceptionMessage.contains("null")) {
            errorMessage = converterErrors.getMessage("GENERIC-002");
        }
        // Violación genérica de integridad
        else {
            errorMessage = converterErrors.getMessage("GENERIC-003");
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(errorMessage)
                .path(request.getDescription(false).replace("uri=", ""))
                .details(details.isEmpty() ? null : details)
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
                .message(converterErrors.getMessage("GENERIC-004"))
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
