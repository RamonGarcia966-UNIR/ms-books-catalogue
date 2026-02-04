package es.unir.dwfs.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans de Spring para el microservicio de catálogo
 */
@Configuration
public class BeanConfig {

    /**
     * Configura el ObjectMapper con soporte para tipos de fecha/hora de Java 8
     * Necesario para serializar/deserializar LocalDate, LocalDateTime, etc.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Registrar módulo para tipos Java 8 date/time (LocalDate, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
