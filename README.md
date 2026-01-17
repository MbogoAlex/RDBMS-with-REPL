# Custom RDBMS - Pesapal Junior Dev Challenge 2026

A fully functional relational database management system built from scratch in Java, featuring SQL parsing, B-tree indexing, and both command-line (REPL) and web interfaces.

## Project Overview

This project implements a custom RDBMS without using any existing database engines (no PostgreSQL, MySQL, or SQLite). All database functionality is built from the ground up, including:

- SQL lexer and parser
- Query execution engine
- File-based storage system
- B-tree indexing
- Schema management with persistence
- Primary key and unique constraints
- JOIN operations (INNER, LEFT, RIGHT)
- Interactive REPL
- REST API
- Web-based interface

## Features

### SQL Support
- CREATE TABLE with multiple column types
- DROP TABLE
- INSERT INTO
- SELECT with WHERE clauses, AND/OR operators
- UPDATE with WHERE conditions
- DELETE with WHERE conditions
- CREATE INDEX (unique and non-unique)
- INNER JOIN, LEFT JOIN, RIGHT JOIN

### Data Types
- INT - 32-bit integer
- LONG - 64-bit integer
- VARCHAR(n) - Variable-length string
- BOOLEAN - True/false values
- DATE - Date values
- DATETIME - Date and time
- TIMESTAMP - Unix timestamp

### Constraints
- PRIMARY KEY - Enforced uniqueness
- UNIQUE - Unique constraint validation
- NOT NULL - Null value prevention

### Interfaces
1. Command-line REPL with syntax highlighting
2. REST API for programmatic access
3. Web UI with SQL terminal and GUI mode

## Architecture

```
Query Input (SQL String)
    |
    v
Lexer (Tokenization)
    |
    v
Parser (AST Generation)
    |
    v
Query Engine (Execution)
    |
    v
Storage/Index/Schema Managers
    |
    v
File System (Persistent Storage)
```

### Key Components

**Parser Layer**
- SQLLexer: Converts SQL text into tokens
- SQLParser: Builds Abstract Syntax Tree from tokens
- AST Classes: Represent different SQL statements

**Execution Layer**
- QueryEngine: Executes parsed statements
- QueryResult: Encapsulates execution results

**Storage Layer**
- TableStorage: Handles file I/O operations
- Row: Represents database rows
- Binary file format (.tbl files)

**Schema Layer**
- Schema: Manages database metadata
- SchemaManager: Persists schema to disk
- Table/Column: Define structure

**Index Layer**
- IndexManager: Coordinates all indexes
- BTreeIndex: B-tree implementation using TreeMap
- O(log n) lookup performance

## Setup and Installation

### Prerequisites
- Java 21 or higher
- Gradle 9.2 or higher

### Build Project
```bash
cd duka-backend
./gradlew build
```

### Run Command-Line REPL
```bash
./gradlew runRepl --console=plain
```

### Run Web Application
```bash
./gradlew bootRun
```
Then open browser to: http://localhost:8080

## Usage Examples

### Command-Line REPL

```sql
-- Create a table
CREATE TABLE products (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    price INT,
    stock INT
)

-- Insert data
INSERT INTO products VALUES (1, 'Laptop', 75000, 10)
INSERT INTO products VALUES (2, 'Mouse', 1500, 50)

-- Query data
SELECT * FROM products
SELECT * FROM products WHERE price > 10000

-- Update data
UPDATE products SET stock = 100 WHERE id = 2

-- Create index for faster queries
CREATE INDEX idx_price ON products (price)

-- Delete data
DELETE FROM products WHERE stock < 5

-- Meta commands
SHOW TABLES
DESCRIBE products

-- Exit
exit
```

### REST API

**Execute SQL:**
```bash
curl -X POST http://localhost:8080/api/execute \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT * FROM products"}'
```

**List Tables:**
```bash
curl http://localhost:8080/api/tables
```

**Get Table Schema:**
```bash
# Replace {tableName} with your actual table name
curl http://localhost:8080/api/tables/{tableName}/schema

# Example:
curl http://localhost:8080/api/tables/books/schema
```

**Health Check:**
```bash
curl http://localhost:8080/api/health
```

## Data Persistence

All data is stored in the `data/` directory:
- `schema.meta` - Table definitions and structure
- `*.tbl` - Binary files containing table data

Data persists across application restarts. To reset:
```bash
rm -rf data/
```

## Testing

### Run Automated Tests
```bash
./gradlew test
```

### Manual Testing
1. Start REPL: `./gradlew runRepl --console=plain`
2. Run demo script: `./gradlew runRepl --console=plain < demo.sql`
3. Start web UI: `./gradlew bootRun` and visit http://localhost:8080

## Web Application Demo

The web interface demonstrates CRUD operations through:

1. **SQL Terminal Tab**
   - Interactive command-line in browser
   - Command history (use arrow keys)
   - Pretty-printed results
   - Execution timing

2. **GUI Mode Tab**
   - Visual table creation with form
   - Add/remove columns dynamically
   - Insert data through generated forms
   - View and manage table data
   - Drop tables and delete data

3. **Query Builder Tab**
   - Select table from dropdown
   - Specify columns and WHERE conditions
   - Execute queries visually

## Project Structure

```
src/main/java/com/rdmbs/rdbms/
├── controller/          REST API endpoints
├── service/             Business logic
├── dto/                 Request/Response objects
└── rdbms/
    ├── parser/          SQL lexer and parser
    ├── engine/          Query execution
    ├── storage/         File I/O operations
    ├── schema/          Metadata management
    ├── index/           B-tree indexing
    └── repl/            Command-line interface

src/main/resources/static/
├── index.html           Web UI
├── css/styles.css       Styling
└── js/app.js            Frontend logic
```

## Technical Details

### Storage Format
- Binary files for efficiency
- Type-aware serialization using DataInputStream/DataOutputStream
- One file per table

### Indexing
- TreeMap-based B-tree (Red-Black tree internally)
- Automatic balancing
- Support for range queries

### Query Execution
- AST-based execution
- Sequential table scans
- Index utilization when available
- Nested loop joins

## Limitations

- Single database (no CREATE DATABASE command)
- No transaction support
- No concurrent access control
- Limited aggregate functions
- No subqueries
- Sequential scans for non-indexed queries

## Future Enhancements

- Transaction support (BEGIN, COMMIT, ROLLBACK)
- Write-Ahead Logging (WAL)
- Query optimizer
- Aggregate functions (COUNT, SUM, AVG, MIN, MAX)
- GROUP BY and HAVING
- Subqueries
- Multiple databases
- Connection pooling
- User authentication

## Development Notes

This project was developed for the Pesapal Junior Developer Challenge 2026. The goal was to demonstrate understanding of database fundamentals by building a complete RDBMS from scratch.

### Tools Used
- Java 21
- Spring Boot 4.0
- Gradle 9.2
- Lombok (for reducing boilerplate)

### AI Assistance
This project was developed with assistance from GitHub Copilot CLI for:
- Code suggestions and boilerplate reduction
- Documentation generation
- Debugging assistance

All core logic, architecture decisions, and implementation were done by the developer.

## License

This project is submitted for the Pesapal Junior Developer Challenge 2026.

## Author

[Alex Mbogo]
[mbogoalex3@gmail.com]
[https://github.com/MbogoAlex]

## Submission Date

January 17, 2026
