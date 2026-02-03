-- Datos de prueba para el catálogo de libros
-- Libros visibles
INSERT INTO books (title, author, publication_date, category, isbn, rating, price, visible) VALUES
('Cien años de soledad', 'Gabriel García Márquez', '1967-05-30', 'Ficción', '978-0307474728', 5, 19.99, true),
('Don Quijote de la Mancha', 'Miguel de Cervantes', '1605-01-16', 'Clásico', '978-8424936464', 5, 24.50, true),
('La sombra del viento', 'Carlos Ruiz Zafón', '2001-04-17', 'Misterio', '978-8408163381', 4, 18.95, true),
('Rayuela', 'Julio Cortázar', '1963-06-28', 'Ficción', '978-8420471891', 4, 22.00, true),
('El amor en los tiempos del cólera', 'Gabriel García Márquez', '1985-09-05', 'Romance', '978-0307389732', 5, 21.50, true),
('La casa de los espíritus', 'Isabel Allende', '1982-10-01', 'Ficción', '978-8401337208', 4, 20.00, true),
('Ficciones', 'Jorge Luis Borges', '1944-01-01', 'Ficción', '978-8420633886', 5, 16.95, true),
('El túnel', 'Ernesto Sabato', '1948-01-01', 'Ficción', '978-8432217326', 4, 14.50, true);

-- Libros ocultos (no disponibles para compra)
INSERT INTO books (title, author, publication_date, category, isbn, rating, price, visible) VALUES
('Crónica de una muerte anunciada', 'Gabriel García Márquez', '1981-04-01', 'Ficción', '978-0307387493', 4, 17.99, false),
('Pedro Páramo', 'Juan Rulfo', '1955-03-19', 'Ficción', '978-8420633695', 5, 15.00, false);
