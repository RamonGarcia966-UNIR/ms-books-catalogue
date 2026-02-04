package es.unir.dwfs.catalogue.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Utilidad para resolver códigos de error a mensajes descriptivos
 */
@Component
@RequiredArgsConstructor
public class ConverterErrors {

    private final MessageSource messageSource;

    /**
     * Obtiene el mensaje asociado a un código de error
     */
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, Locale.ROOT);
    }

    /**
     * Obtiene el mensaje asociado a un código de error con argumentos
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.ROOT);
    }
}
