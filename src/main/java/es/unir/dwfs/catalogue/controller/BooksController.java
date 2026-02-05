package es.unir.dwfs.catalogue.controller;

import es.unir.dwfs.catalogue.controller.model.BookDto;
import es.unir.dwfs.catalogue.controller.model.CreateBookRequest;
import es.unir.dwfs.catalogue.data.model.Book;
import es.unir.dwfs.catalogue.exception.ConverterErrors;
import es.unir.dwfs.catalogue.exception.ErrorResponse;
import es.unir.dwfs.catalogue.service.BooksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final ConverterErrors converterErrors;

    @GetMapping("/books")
    @Operation(summary = "Obtener libros", description = "Obtiene todos los libros del catálogo o filtra por criterios de búsqueda combinados (título, autor, categoría, ISBN, rating, precio, visibilidad)", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libros encontrados y devueltos exitosamente"),
            @ApiResponse(responseCode = "500", description = """
                    Internal Server Error - Error inesperado del servidor:

                    - **GENERIC-004**: Ha ocurrido un error inesperado. Por favor, contacte al administrador
                    """)
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
            @ApiResponse(responseCode = "500", description = """
                    Internal Server Error - Error inesperado del servidor:

                    - **GENERIC-004**: Ha ocurrido un error inesperado. Por favor, contacte al administrador
                    """)
    })
    public ResponseEntity<?> getBook(@PathVariable String bookId, HttpServletRequest request) {

        log.info("Request to get book with id: {}", bookId);

        Book book = service.getBook(bookId);

        if (book != null) {
            return ResponseEntity.ok(book);
        } else {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(converterErrors.getMessage("BOOK-404-001"))
                    .path(request.getRequestURI())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/books/{bookId}")
    @Operation(summary = "Eliminar libro", description = "Elimina un libro del catálogo mediante su identificador único", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libro eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Not Found - No existe un libro con el ID especificado"),
            @ApiResponse(responseCode = "500", description = """
                    Internal Server Error - Error inesperado del servidor:

                    - **GENERIC-004**: Ha ocurrido un error inesperado. Por favor, contacte al administrador
                    """)
    })
    public ResponseEntity<?> deleteBook(@PathVariable String bookId, HttpServletRequest request) {

        log.info("Request to delete book with id: {}", bookId);

        boolean removed = service.removeBook(bookId);

        if (removed) {
            return ResponseEntity.ok().build();
        } else {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(converterErrors.getMessage("BOOK-404-001"))
                    .path(request.getRequestURI())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/books")
    @Operation(summary = "Crear libro", description = "Crea un nuevo libro en el catálogo", responses = {
            @ApiResponse(responseCode = "201", description = "Created - Libro creado exitosamente"),
            @ApiResponse(responseCode = "400", description = """
                    Bad Request - La petición contiene errores de formato, sintaxis o validación de datos que impiden su procesamiento. Los errores por validación de datos vienen detallados en el atributo 'details' de la respuesta y pueden ser:

                    **Campo 'title':**
                    - **BOOK-001**: El parámetro 'title' es obligatorio y no puede estar vacío
                    - **BOOK-002**: El parámetro 'title' ha superado el tamaño máximo permitido (max: 200 caracteres)

                    **Campo 'author':**
                    - **BOOK-010**: El parámetro 'author' es obligatorio y no puede estar vacío
                    - **BOOK-011**: El parámetro 'author' ha superado el tamaño máximo permitido (max: 150 caracteres)

                    **Campo 'isbn':**
                    - **BOOK-020**: El parámetro 'isbn' ha superado el tamaño máximo permitido (max: 20 caracteres)
                    - **BOOK-021**: El parámetro 'isbn' tiene un formato no válido

                    **Campo 'category':**
                    - **BOOK-030**: El parámetro 'category' ha superado el tamaño máximo permitido (max: 100 caracteres)

                    **Campo 'price':**
                    - **BOOK-040**: El parámetro 'price' es obligatorio y no puede estar vacío
                    - **BOOK-041**: El parámetro 'price' debe ser mayor o igual a 0
                    - **BOOK-042**: El parámetro 'price' debe tener como máximo 2 decimales

                    **Campo 'visible':**
                    - **BOOK-060**: El parámetro 'visible' es obligatorio y no puede estar vacío

                    **Campo 'publicationDate':**
                    - **BOOK-070**: El parámetro 'publicationDate' es obligatorio y no puede estar vacío
                    - **BOOK-072**: El parámetro 'publicationDate' no puede ser una fecha futura
                    """),
            @ApiResponse(responseCode = "409", description = """
                    Conflict - Violación de restricción de integridad de datos. Los errores pueden ser:

                    **Campo 'isbn':**
                    - **BOOK-022**: El parámetro 'isbn' ya existe en el sistema

                    **Errores genéricos:**
                    - **GENERIC-001**: Ya existe un registro con el mismo identificador
                    - **GENERIC-002**: Faltan campos obligatorios
                    - **GENERIC-003**: Error de integridad de datos
                    """),
            @ApiResponse(responseCode = "500", description = """
                    Internal Server Error - Error inesperado del servidor:

                    - **GENERIC-004**: Ha ocurrido un error inesperado. Por favor, contacte al administrador
                    """)
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
    @Operation(summary = "Actualizar libro parcialmente", description = "Actualiza parcialmente un libro existente usando JSON Merge Patch (RFC 7386)", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libro actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = """
                    Bad Request - La petición contiene errores de formato, sintaxis o validación de datos que impiden su procesamiento. Los errores por validación de datos vienen detallados en el atributo 'details' de la respuesta y pueden ser:

                    **Campo 'title':**
                    - **BOOK-001**: El parámetro 'title' es obligatorio y no puede estar vacío
                    - **BOOK-002**: El parámetro 'title' ha superado el tamaño máximo permitido (max: 200 caracteres)

                    **Campo 'author':**
                    - **BOOK-010**: El parámetro 'author' es obligatorio y no puede estar vacío
                    - **BOOK-011**: El parámetro 'author' ha superado el tamaño máximo permitido (max: 150 caracteres)

                    **Campo 'isbn':**
                    - **BOOK-020**: El parámetro 'isbn' ha superado el tamaño máximo permitido (max: 20 caracteres)
                    - **BOOK-021**: El parámetro 'isbn' tiene un formato no válido

                    **Campo 'category':**
                    - **BOOK-030**: El parámetro 'category' ha superado el tamaño máximo permitido (max: 100 caracteres)

                    **Campo 'price':**
                    - **BOOK-040**: El parámetro 'price' es obligatorio y no puede estar vacío
                    - **BOOK-041**: El parámetro 'price' debe ser mayor o igual a 0
                    - **BOOK-042**: El parámetro 'price' debe tener como máximo 2 decimales

                    **Campo 'visible':**
                    - **BOOK-060**: El parámetro 'visible' es obligatorio y no puede estar vacío

                    **Campo 'publicationDate':**
                    - **BOOK-070**: El parámetro 'publicationDate' es obligatorio y no puede estar vacío
                    - **BOOK-072**: El parámetro 'publicationDate' no puede ser una fecha futura
                    """),
            @ApiResponse(responseCode = "404", description = "Not Found - No existe un libro con el ID especificado"),
            @ApiResponse(responseCode = "409", description = """
                    Conflict - Violación de restricción de integridad de datos. Los errores pueden ser:

                    **Campo 'isbn':**
                    - **BOOK-022**: El parámetro 'isbn' ya existe en el sistema

                    **Errores genéricos:**
                    - **GENERIC-001**: Ya existe un registro con el mismo identificador
                    - **GENERIC-002**: Faltan campos obligatorios
                    - **GENERIC-003**: Error de integridad de datos
                    """),
            @ApiResponse(responseCode = "500", description = """
                    Internal Server Error - Error inesperado del servidor:

                    - **GENERIC-004**: Ha ocurrido un error inesperado. Por favor, contacte al administrador
                    """)
    })
    public ResponseEntity<?> patchBook(@PathVariable String bookId, @RequestBody String patchBody,
            HttpServletRequest request) {

        log.info("Request to patch book with id: {}", bookId);

        Book patchedBook = service.updateBook(bookId, patchBody);

        if (patchedBook != null) {
            return ResponseEntity.ok(patchedBook);
        } else {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(converterErrors.getMessage("BOOK-404-001"))
                    .path(request.getRequestURI())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/books/{bookId}")
    @Operation(summary = "Actualizar libro completamente", description = "Actualiza todos los campos de un libro existente", responses = {
            @ApiResponse(responseCode = "200", description = "OK - Libro actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = """
                    Bad Request - La petición contiene errores de formato, sintaxis o validación de datos que impiden su procesamiento. Los errores por validación de datos vienen detallados en el atributo 'details' de la respuesta y pueden ser:

                    **Campo 'title':**
                    - **BOOK-001**: El parámetro 'title' es obligatorio y no puede estar vacío
                    - **BOOK-002**: El parámetro 'title' ha superado el tamaño máximo permitido (max: 200 caracteres)

                    **Campo 'author':**
                    - **BOOK-010**: El parámetro 'author' es obligatorio y no puede estar vacío
                    - **BOOK-011**: El parámetro 'author' ha superado el tamaño máximo permitido (max: 150 caracteres)

                    **Campo 'isbn':**
                    - **BOOK-020**: El parámetro 'isbn' ha superado el tamaño máximo permitido (max: 20 caracteres)
                    - **BOOK-021**: El parámetro 'isbn' tiene un formato no válido

                    **Campo 'category':**
                    - **BOOK-030**: El parámetro 'category' ha superado el tamaño máximo permitido (max: 100 caracteres)

                    **Campo 'price':**
                    - **BOOK-040**: El parámetro 'price' es obligatorio y no puede estar vacío
                    - **BOOK-041**: El parámetro 'price' debe ser mayor o igual a 0
                    - **BOOK-042**: El parámetro 'price' debe tener como máximo 2 decimales

                    **Campo 'visible':**
                    - **BOOK-060**: El parámetro 'visible' es obligatorio y no puede estar vacío

                    **Campo 'publicationDate':**
                    - **BOOK-070**: El parámetro 'publicationDate' es obligatorio y no puede estar vacío
                    - **BOOK-072**: El parámetro 'publicationDate' no puede ser una fecha futura
                    """),
            @ApiResponse(responseCode = "404", description = "Not Found - No existe un libro con el ID especificado"),
            @ApiResponse(responseCode = "409", description = """
                    Conflict - Violación de restricción de integridad de datos. Los errores pueden ser:

                    **Campo 'isbn':**
                    - **BOOK-022**: El parámetro 'isbn' ya existe en el sistema

                    **Errores genéricos:**
                    - **GENERIC-001**: Ya existe un registro con el mismo identificador
                    - **GENERIC-002**: Faltan campos obligatorios
                    - **GENERIC-003**: Error de integridad de datos
                    """),
            @ApiResponse(responseCode = "500", description = """
                    Internal Server Error - Error inesperado del servidor:

                    - **GENERIC-004**: Ha ocurrido un error inesperado. Por favor, contacte al administrador
                    """)
    })
    public ResponseEntity<?> updateBook(@PathVariable String bookId, @Valid @RequestBody BookDto body,
            HttpServletRequest request) {

        log.info("Request to update book with id: {}", bookId);

        Book updatedBook = service.updateBook(bookId, body);

        if (updatedBook != null) {
            return ResponseEntity.ok(updatedBook);
        } else {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(converterErrors.getMessage("BOOK-404-001"))
                    .path(request.getRequestURI())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
