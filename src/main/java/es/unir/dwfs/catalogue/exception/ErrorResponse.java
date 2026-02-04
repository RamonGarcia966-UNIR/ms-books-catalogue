package es.unir.dwfs.catalogue.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase DTO para representar respuestas de error consistentes en la API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Fecha y hora del error
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP
     */
    private int status;

    /**
     * Tipo de error (ej. "Bad Request", "Conflict")
     */
    private String error;

    /**
     * Mensaje descriptivo del error
     */
    private String message;

    /**
     * Ruta de la petición
     */
    private String path;

    /**
     * Detalles de los errores de validación
     */
    private List<ErrorDetail> details;

    /**
     * Clase interna para representar detalles de errores de validación
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetail {
        /**
         * Campo que falló la validación
         */
        private String element;

        /**
         * Código del error
         */
        private String code;

        /**
         * Descripción legible del error
         */
        private String description;
    }
}
