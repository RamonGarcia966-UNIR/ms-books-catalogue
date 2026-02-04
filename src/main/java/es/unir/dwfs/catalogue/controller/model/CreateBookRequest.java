package es.unir.dwfs.catalogue.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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

    @NotBlank(message = "BOOK-001")
    @Length(max = 200, message = "BOOK-002")
    private String title;

    @NotBlank(message = "BOOK-010")
    @Length(max = 150, message = "BOOK-011")
    private String author;

    @NotNull(message = "BOOK-070")
    @PastOrPresent(message = "BOOK-072")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    @Length(max = 100, message = "BOOK-030")
    private String category;

    @Length(max = 20, message = "BOOK-020")
    @Pattern(regexp = "^[0-9\\-]{10,17}$", message = "BOOK-021")
    private String isbn;

    @Min(value = 0, message = "BOOK-050")
    @Max(value = 5, message = "BOOK-050")
    private Integer rating;

    @NotNull(message = "BOOK-040")
    @DecimalMin(value = "0.0", message = "BOOK-041")
    @Digits(integer = 10, fraction = 2, message = "BOOK-042")
    private BigDecimal price;

    @NotNull(message = "BOOK-060")
    private Boolean visible;
}
