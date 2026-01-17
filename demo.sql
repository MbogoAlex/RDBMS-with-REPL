CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100), price INT, stock INT, category VARCHAR(50))
INSERT INTO products VALUES (1, 'Laptop', 75000, 10, 'Electronics')
INSERT INTO products VALUES (2, 'Mouse', 1500, 50, 'Electronics')
INSERT INTO products VALUES (3, 'Keyboard', 3000, 30, 'Electronics')
SELECT * FROM products
SELECT * FROM products WHERE price > 10000
UPDATE products SET stock = 100 WHERE id = 2
SELECT * FROM products WHERE id = 2
CREATE TABLE sales (id INT PRIMARY KEY, product_id INT, quantity INT, total_amount INT)
INSERT INTO sales VALUES (1, 1, 2, 150000)
SELECT * FROM sales
exit
