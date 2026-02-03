package es.unir.dwfs.catalogue.data.model;

import es.unir.dwfs.catalogue.controller.model.BookDto;
import es.unir.dwfs.catalogue.data.utils.Consts;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA para libros del catálogo
 */
@Entity
@Table(name = "books")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = Consts.TITLE, nullable = false)
    private String title;

    @Column(name = Consts.AUTHOR, nullable = false)
    private String author;

    @Column(name = Consts.PUBLICATION_DATE)
    private LocalDate publicationDate;

    @Column(name = Consts.CATEGORY)
    private String category;

    @Column(name = Consts.ISBN, unique = true)
    private String isbn;

    @Column(name = Consts.RATING)
    private Integer rating;

    @Column(name = Consts.PRICE, nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = Consts.VISIBLE, nullable = false)
    private Boolean visible;

    /**
     * Actualiza los campos del libro con los valores del DTO
     */
    public void update(BookDto bookDto) {
        if (bookDto.getTitle() != null) {
            this.title = bookDto.getTitle();
        }
        if (bookDto.getAuthor() != null) {
            this.author = bookDto.getAuthor();
        }
        if (bookDto.getPublicationDate() != null) {
            this.publicationDate = bookDto.getPublicationDate();
        }
        if (bookDto.getCategory() != null) {
            this.category = bookDto.getCategory();
        }
        if (bookDto.getIsbn() != null) {
            this.isbn = bookDto.getIsbn();
        }
        if (bookDto.getRating() != null) {
            this.rating = bookDto.getRating();
        }
        if (bookDto.getPrice() != null) {
            this.price = bookDto.getPrice();
        }
        if (bookDto.getVisible() != null) {
            this.visible = bookDto.getVisible();
        }
    }
}
