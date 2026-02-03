package es.unir.dwfs.catalogue.controller.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request para crear un nuevo libro
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CreateBookRequest {

    private String title;
    private String author;
    private LocalDate publicationDate;
    private String category;
    private String isbn;
    private Integer rating;
    private BigDecimal price;
    private Boolean visible;
}
