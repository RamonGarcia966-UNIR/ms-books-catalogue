package es.unir.dwfs.catalogue.service;

import es.unir.dwfs.catalogue.controller.model.BookDto;
import es.unir.dwfs.catalogue.controller.model.CreateBookRequest;
import es.unir.dwfs.catalogue.data.model.Book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz del servicio de libros
 */
public interface BooksService {

    List<Book> getBooks(String title, String author, LocalDate publicationDate,
            String category, String isbn, Integer rating, BigDecimal price, Boolean visible);

    Book getBook(String bookId);

    boolean removeBook(String bookId);

    Book createBook(CreateBookRequest request);

    Book updateBook(String bookId, String patchBody);

    Book updateBook(String bookId, BookDto updateRequest);
}
