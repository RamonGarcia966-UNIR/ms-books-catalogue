package es.unir.dwfs.catalogue.data;

import es.unir.dwfs.catalogue.data.model.Book;
import es.unir.dwfs.catalogue.data.utils.SearchCriteria;
import es.unir.dwfs.catalogue.data.utils.SearchOperation;
import es.unir.dwfs.catalogue.data.utils.SearchStatement;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio custom con búsqueda dinámica
 */
@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final BookJpaRepository repository;

    /**
     * Obtiene todos los libros
     */
    public List<Book> getBooks() {
        return repository.findAll();
    }

    /**
     * Obtiene un libro por ID
     */
    public Book getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * Guarda un libro
     */
    public Book save(Book book) {
        return repository.save(book);
    }

    /**
     * Elimina un libro
     */
    public void delete(Book book) {
        repository.delete(book);
    }

    /**
     * Búsqueda dinámica por múltiples criterios
     */
    public List<Book> search(String title, String author, LocalDate publicationDate,
            String category, String isbn, Integer rating, BigDecimal price, Boolean visible) {

        SearchStatement titleSpec = title != null
                ? new SearchStatement(new SearchCriteria("title", title, SearchOperation.LIKE))
                : null;

        SearchStatement authorSpec = author != null
                ? new SearchStatement(new SearchCriteria("author", author, SearchOperation.LIKE))
                : null;

        SearchStatement publicationDateSpec = publicationDate != null
                ? new SearchStatement(new SearchCriteria("publicationDate", publicationDate, SearchOperation.EQUAL))
                : null;

        SearchStatement categorySpec = category != null
                ? new SearchStatement(new SearchCriteria("category", category, SearchOperation.LIKE))
                : null;

        SearchStatement isbnSpec = isbn != null
                ? new SearchStatement(new SearchCriteria("isbn", isbn, SearchOperation.EQUAL))
                : null;

        SearchStatement ratingSpec = rating != null
                ? new SearchStatement(new SearchCriteria("rating", rating, SearchOperation.EQUAL))
                : null;

        SearchStatement priceSpec = price != null
                ? new SearchStatement(new SearchCriteria("price", price, SearchOperation.EQUAL))
                : null;

        SearchStatement visibleSpec = visible != null
                ? new SearchStatement(new SearchCriteria("visible", visible, SearchOperation.EQUAL))
                : null;

        // Inicializar especificación combinada
        Specification<Book> combinedSpec = titleSpec != null ? Specification.where(titleSpec) : null;

        if (authorSpec != null)
            combinedSpec = combinedSpec == null ? Specification.where(authorSpec) : combinedSpec.and(authorSpec);
        if (publicationDateSpec != null)
            combinedSpec = combinedSpec == null ? Specification.where(publicationDateSpec)
                    : combinedSpec.and(publicationDateSpec);
        if (categorySpec != null)
            combinedSpec = combinedSpec == null ? Specification.where(categorySpec) : combinedSpec.and(categorySpec);
        if (isbnSpec != null)
            combinedSpec = combinedSpec == null ? Specification.where(isbnSpec) : combinedSpec.and(isbnSpec);
        if (ratingSpec != null)
            combinedSpec = combinedSpec == null ? Specification.where(ratingSpec) : combinedSpec.and(ratingSpec);
        if (priceSpec != null)
            combinedSpec = combinedSpec == null ? Specification.where(priceSpec) : combinedSpec.and(priceSpec);
        if (visibleSpec != null)
            combinedSpec = combinedSpec == null ? Specification.where(visibleSpec) : combinedSpec.and(visibleSpec);

        if (combinedSpec != null) {
            return repository.findAll(combinedSpec);
        }

        return repository.findAll();
    }
}
