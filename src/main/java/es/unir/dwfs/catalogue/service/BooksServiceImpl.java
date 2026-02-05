package es.unir.dwfs.catalogue.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import es.unir.dwfs.catalogue.controller.model.BookDto;
import es.unir.dwfs.catalogue.controller.model.CreateBookRequest;
import es.unir.dwfs.catalogue.data.BookRepository;
import es.unir.dwfs.catalogue.data.model.Book;
import es.unir.dwfs.catalogue.exception.BusinessRuleViolationException;
import es.unir.dwfs.catalogue.exception.ConverterErrors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio de libros
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BooksServiceImpl implements BooksService {

    private final BookRepository repository;
    private final ObjectMapper objectMapper;
    private final ConverterErrors converterErrors;

    @Override
    public List<Book> getBooks(String title, String author, LocalDate publicationDate,
            String category, String isbn, Integer rating, BigDecimal price, Boolean visible) {

        if (StringUtils.hasLength(title) || StringUtils.hasLength(author) || publicationDate != null
                || StringUtils.hasLength(category) || StringUtils.hasLength(isbn)
                || rating != null || price != null || visible != null) {
            return repository.search(title, author, publicationDate, category, isbn, rating, price, visible);
        }

        List<Book> books = repository.getBooks();
        return books.isEmpty() ? null : books;
    }

    @Override
    public Book getBook(String bookId) {
        return repository.getById(Long.valueOf(bookId));
    }

    @Override
    public boolean removeBook(String bookId) {
        Book book = repository.getById(Long.valueOf(bookId));

        if (book != null) {
            repository.delete(book);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Book createBook(CreateBookRequest request) {

        if (request != null && StringUtils.hasLength(request.getTitle())
                && StringUtils.hasLength(request.getAuthor())
                && request.getPrice() != null && request.getVisible() != null) {

            Book book = Book.builder()
                    .title(request.getTitle())
                    .author(request.getAuthor())
                    .publicationDate(request.getPublicationDate())
                    .category(request.getCategory())
                    .isbn(request.getIsbn())
                    .rating(request.getRating())
                    .price(request.getPrice())
                    .visible(request.getVisible())
                    .build();

            // Validación de precio
            validatePrice(book.getPrice());

            return repository.save(book);
        } else {
            return null;
        }
    }

    @Override
    public Book updateBook(String bookId, String patchBody) {

        // PATCH se implementa mediante Merge Patch:
        // https://datatracker.ietf.org/doc/html/rfc7386
        Book book = repository.getById(Long.valueOf(bookId));
        if (book != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(patchBody));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(book)));
                Book patched = objectMapper.treeToValue(target, Book.class);

                // Validación manual post-patch
                validatePrice(patched.getPrice());

                return repository.save(patched);
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating book {}", bookId, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Book updateBook(String bookId, BookDto updateRequest) {
        Book book = repository.getById(Long.valueOf(bookId));
        if (book != null) {
            book.update(updateRequest);

            // Validación manual post-update
            validatePrice(book.getPrice());

            return repository.save(book);
        } else {
            return null;
        }
    }

    /**
     * Valida que el precio sea mayor que 0
     */
    private void validatePrice(BigDecimal price) {
        log.info("Validating price: {}", price);
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Price validation failed for value: {}", price);
            throw new BusinessRuleViolationException(
                    "Error de validación de negocio",
                    "BOOK-041",
                    converterErrors.getMessage("BOOK-041"));
        }
    }

}
