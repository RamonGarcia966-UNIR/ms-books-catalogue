package es.unir.dwfs.catalogue.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Excepción para violaciones de reglas de negocio (HTTP 422)
 * No usa ConstraintViolation para evitar acoplamiento con Bean Validation
 */
@Getter
public class BusinessRuleViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<ErrorResponse.ErrorDetail> errors;

    public BusinessRuleViolationException(String message, List<ErrorResponse.ErrorDetail> errors) {
        super(message);
        this.errors = errors;
    }

    public BusinessRuleViolationException(String message, ErrorResponse.ErrorDetail error) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(error);
    }

    public BusinessRuleViolationException(String message, String code, String description) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(ErrorResponse.ErrorDetail.builder()
                .code(code)
                .description(description)
                .build());
    }
}
