package es.unir.dwfs.catalogue.controller;

import es.unir.dwfs.catalogue.controller.model.BookDto;
import es.unir.dwfs.catalogue.controller.model.CreateBookRequest;
import es.unir.dwfs.catalogue.data.model.Book;
import es.unir.dwfs.catalogue.service.BooksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de libros
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books Controller", description = "API REST para gestión del catálogo de libros. Incluye operaciones CRUD completas con manejo robusto de errores (409 Conflict para violaciones de integridad, 500 para errores del servidor)")

public class BooksController {

    private final BooksService service;

    @GetMapping("/books")
    @Operation(summary = "Obtener libros", description = "Obtiene todos los libros del catálogo o filtra por criterios de búsqueda combinados (título, autor, categoría, ISBN, rating, precio, visibilidad)", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libros encontrados y devueltos exitosamente"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Error inesperado del servidor")
    })
    public ResponseEntity<List<Book>> getBooks(
            @RequestHeader Map<String, String> headers,
            @Parameter(description = "Título del libro", example = "Cien años de soledad") @RequestParam(required = false) String title,
            @Parameter(description = "Autor del libro", example = "Gabriel García Márquez") @RequestParam(required = false) String author,
            @Parameter(description = "Fecha de publicación", example = "1967-05-30") @RequestParam(required = false) LocalDate publicationDate,
            @Parameter(description = "Categoría del libro", example = "Ficción") @RequestParam(required = false) String category,
            @Parameter(description = "Código ISBN", example = "978-0307474728") @RequestParam(required = false) String isbn,
            @Parameter(description = "Valoración (1-5)", example = "5") @RequestParam(required = false) Integer rating,
            @Parameter(description = "Precio del libro", example = "19.99") @RequestParam(required = false) BigDecimal price,
            @Parameter(description = "Visibilidad del libro", example = "true") @RequestParam(required = false) Boolean visible) {

        log.info(
                "Request to get books with filters - title: {}, author: {}, category: {}, isbn: {}, rating: {}, price: {}, visible: {}",
                title, author, category, isbn, rating, price, visible);

        List<Book> books = service.getBooks(title, author, publicationDate, category, isbn, rating, price, visible);

        if (books != null && !books.isEmpty()) {
            return ResponseEntity.ok(books);
        } else {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/books/{bookId}")
    @Operation(summary = "Obtener libro por ID", description = "Obtiene un libro específico del catálogo mediante su identificador único", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libro encontrado y devuelto exitosamente"),
            @ApiResponse(responseCode = "404", description = "Not Found - No existe un libro con el ID especificado"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Error inesperado del servidor")
    })
    public ResponseEntity<Book> getBook(@PathVariable String bookId) {

        log.info("Request to get book with id: {}", bookId);

        Book book = service.getBook(bookId);

        if (book != null) {
            return ResponseEntity.ok(book);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/books/{bookId}")
    @Operation(summary = "Eliminar libro", description = "Elimina un libro del catálogo mediante su identificador único", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libro eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Not Found - No existe un libro con el ID especificado"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Error inesperado del servidor")
    })
    public ResponseEntity<Void> deleteBook(@PathVariable String bookId) {

        log.info("Request to delete book with id: {}", bookId);

        boolean removed = service.removeBook(bookId);

        if (removed) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/books")
    @Operation(summary = "Crear libro", description = "Crea un nuevo libro en el catálogo. Campos obligatorios: título, autor, precio y visibilidad", responses = {
            @ApiResponse(responseCode = "201", description = "Created - Libro creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Datos inválidos o faltan campos obligatorios"),
            @ApiResponse(responseCode = "409", description = "Conflict - El ISBN ya existe en el sistema o violación de restricción de integridad"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Error inesperado del servidor")
    })
    public ResponseEntity<Book> addBook(@Valid @RequestBody CreateBookRequest request) {

        log.info("Request to create book: {}", request);

        Book createdBook = service.createBook(request);

        if (createdBook != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/books/{bookId}")
    @Operation(summary = "Actualizar libro parcialmente (PATCH)", description = "Actualiza parcialmente un libro existente usando JSON Merge Patch (RFC 7386). Solo se modifican los campos especificados en el cuerpo de la petición", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libro actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Error en el formato del patch o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Not Found - No existe un libro con el ID especificado"),
            @ApiResponse(responseCode = "409", description = "Conflict - El ISBN modificado ya existe o violación de restricción de integridad"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Error inesperado del servidor")
    })
    public ResponseEntity<Book> patchBook(@PathVariable String bookId, @RequestBody String patchBody) {

        log.info("Request to patch book with id: {}", bookId);

        Book patchedBook = service.updateBook(bookId, patchBody);

        if (patchedBook != null) {
            return ResponseEntity.ok(patchedBook);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/books/{bookId}")
    @Operation(summary = "Actualizar libro completamente (PUT)", description = "Actualiza todos los campos de un libro existente. Se deben proporcionar todos los campos del libro", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libro actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Datos inválidos o faltan campos obligatorios"),
            @ApiResponse(responseCode = "404", description = "Not Found - No existe un libro con el ID especificado"),
            @ApiResponse(responseCode = "409", description = "Conflict - El ISBN modificado ya existe o violación de restricción de integridad"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error - Error inesperado del servidor")
    })
    public ResponseEntity<Book> updateBook(@PathVariable String bookId, @RequestBody BookDto body) {

        log.info("Request to update book with id: {}", bookId);

        Book updatedBook = service.updateBook(bookId, body);

        if (updatedBook != null) {
            return ResponseEntity.ok(updatedBook);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
