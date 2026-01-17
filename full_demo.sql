SHOW TABLES
CREATE TABLE books (id INT PRIMARY KEY, title VARCHAR(100), author VARCHAR(100), year INT, price INT)
SHOW TABLES
DESCRIBE books
INSERT INTO books VALUES (1, 'Things Fall Apart', 'Chinua Achebe', 1958, 1200)
INSERT INTO books VALUES (2, 'Half of a Yellow Sun', 'Chimamanda Adichie', 2006, 1500)
INSERT INTO books VALUES (3, 'Petals of Blood', 'Ngugi wa Thiongo', 1977, 1800)
SELECT * FROM books
SELECT title, author FROM books WHERE year > 1970
UPDATE books SET price = 2000 WHERE id = 3
SELECT * FROM books WHERE id = 3
CREATE INDEX idx_year ON books (year)
SHOW TABLES
DESC books
exit
