#!/bin/bash
# Interactive REPL Demo Script for Duka RDBMS

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║  Duka RDBMS - Interactive REPL Demonstration                ║"
echo "║  Press Enter to see each SQL command execute...             ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Function to run SQL and wait
run_sql() {
    echo "💡 SQL: $1"
    echo "$1" | ./gradlew runRepl --console=plain --quiet
    echo ""
    read -p "Press Enter to continue..."
    echo ""
}

cd "$(dirname "$0")"

echo "📋 Step 1: Creating a products table..."
read -p "Press Enter to continue..."
run_sql "CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100), price INT, stock INT, category VARCHAR(50))"

echo "📋 Step 2: Inserting product data..."
read -p "Press Enter to continue..."
run_sql "INSERT INTO products VALUES (1, 'Laptop', 75000, 10, 'Electronics')"
run_sql "INSERT INTO products VALUES (2, 'Mouse', 1500, 50, 'Electronics')"
run_sql "INSERT INTO products VALUES (3, 'Keyboard', 3000, 30, 'Electronics')"

echo "📋 Step 3: Querying all products..."
read -p "Press Enter to continue..."
run_sql "SELECT * FROM products"

echo "📋 Step 4: Filtering products by price..."
read -p "Press Enter to continue..."
run_sql "SELECT * FROM products WHERE price > 10000"

echo "📋 Step 5: Updating stock quantity..."
read -p "Press Enter to continue..."
run_sql "UPDATE products SET stock = 100 WHERE id = 2"

echo "📋 Step 6: Verifying the update..."
read -p "Press Enter to continue..."
run_sql "SELECT * FROM products WHERE id = 2"

echo "✅ Demo completed successfully!"
