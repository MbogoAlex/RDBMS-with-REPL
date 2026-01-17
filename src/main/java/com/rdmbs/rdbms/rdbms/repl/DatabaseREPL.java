package com.rdmbs.rdbms.rdbms.repl;

import com.rdmbs.rdbms.rdbms.engine.QueryEngine;
import com.rdmbs.rdbms.rdbms.engine.QueryResult;
import com.rdmbs.rdbms.rdbms.index.IndexManager;
import com.rdmbs.rdbms.rdbms.schema.Schema;
import com.rdmbs.rdbms.rdbms.schema.SchemaManager;
import com.rdmbs.rdbms.rdbms.schema.Table;
import com.rdmbs.rdbms.rdbms.schema.Column;
import com.rdmbs.rdbms.rdbms.storage.Row;
import com.rdmbs.rdbms.rdbms.storage.TableStorage;

import java.util.Scanner;

public class DatabaseREPL {
    private final QueryEngine queryEngine;
    private final Scanner scanner;
    private final Schema schema;
    private final TableStorage storage;

    public DatabaseREPL() {
        SchemaManager schemaManager = new SchemaManager();
        Schema loadedSchema;
        
        // Load schema from disk or create new
        try {
            loadedSchema = schemaManager.loadSchema();
        } catch (Exception e) {
            loadedSchema = new Schema();
        }
        
        this.schema = loadedSchema;
        this.storage = new TableStorage();
        IndexManager indexManager = new IndexManager();
        this.queryEngine = new QueryEngine(schema, storage, indexManager);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printWelcome();
        
        while (true) {
            System.out.print("duka> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            // Strip trailing semicolon from ALL input
            input = input.replaceAll(";\\s*$", "").trim();
            
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                break;
            }
            
            if (input.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            
            if (input.equalsIgnoreCase("clear")) {
                clearScreen();
                continue;
            }

            if (input.toUpperCase().equals("SHOW TABLES")) {
                showTables();
                continue;
            }

            if (input.toUpperCase().startsWith("DESCRIBE ") || input.toUpperCase().startsWith("DESC ")) {
                String tableName = input.split("\\s+")[1];
                describeTable(tableName);
                continue;
            }
            
            executeQuery(input);
        }
        
        scanner.close();
    }

    private void executeQuery(String sql) {
        long startTime = System.currentTimeMillis();
        
        QueryResult result = queryEngine.execute(sql);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        if (result.isSuccess()) {
            if (result.getRows() != null && !result.getRows().isEmpty()) {
                printResultSet(result);
            } else {
                System.out.println("✓ " + result.getMessage());
            }
            System.out.println("(" + duration + " ms)");
        } else {
            System.out.println("✗ " + result.getMessage());
        }
        System.out.println();
    }

    private void printResultSet(QueryResult result) {
        if (result.getColumnNames().isEmpty() || result.getRows().isEmpty()) {
            System.out.println("Empty result set");
            return;
        }
        
        int[] columnWidths = calculateColumnWidths(result);
        
        printSeparator(columnWidths);
        printRow(result.getColumnNames().toArray(new String[0]), columnWidths);
        printSeparator(columnWidths);
        
        for (Row row : result.getRows()) {
            String[] values = new String[row.size()];
            for (int i = 0; i < row.size(); i++) {
                Object value = row.getValue(i);
                values[i] = value == null ? "NULL" : value.toString();
            }
            printRow(values, columnWidths);
        }
        
        printSeparator(columnWidths);
        System.out.println(result.getRows().size() + " row(s) returned");
    }

    private int[] calculateColumnWidths(QueryResult result) {
        int[] widths = new int[result.getColumnNames().size()];
        
        for (int i = 0; i < result.getColumnNames().size(); i++) {
            widths[i] = result.getColumnNames().get(i).length();
        }
        
        for (Row row : result.getRows()) {
            for (int i = 0; i < row.size(); i++) {
                Object value = row.getValue(i);
                String strValue = value == null ? "NULL" : value.toString();
                widths[i] = Math.max(widths[i], strValue.length());
            }
        }
        
        for (int i = 0; i < widths.length; i++) {
            widths[i] = Math.min(widths[i] + 2, 50);
        }
        
        return widths;
    }

    private void printRow(String[] values, int[] widths) {
        System.out.print("|");
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (value.length() > widths[i] - 2) {
                value = value.substring(0, widths[i] - 5) + "...";
            }
            System.out.print(" " + padRight(value, widths[i] - 2) + " |");
        }
        System.out.println();
    }

    private void printSeparator(int[] widths) {
        System.out.print("+");
        for (int width : widths) {
            System.out.print("-".repeat(width) + "+");
        }
        System.out.println();
    }

    private String padRight(String str, int length) {
        if (str.length() >= length) return str;
        return str + " ".repeat(length - str.length());
    }

    private void printWelcome() {
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║   Custom RDBMS - Interactive Shell      ║");
        System.out.println("║   Version 1.0                             ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Type 'help' for commands, 'exit' to quit");
        System.out.println();
    }

    private void printHelp() {
        System.out.println("\n=== Available Commands ===");
        System.out.println("  CREATE TABLE <name> (<columns>)  - Create a new table");
        System.out.println("  DROP TABLE <name>                - Drop a table");
        System.out.println("  INSERT INTO <table> VALUES (...)  - Insert data");
        System.out.println("  SELECT <cols> FROM <table>       - Query data");
        System.out.println("  UPDATE <table> SET ...           - Update data");
        System.out.println("  DELETE FROM <table>              - Delete data");
        System.out.println("  CREATE INDEX <name> ON <table>   - Create index");
        System.out.println();
        System.out.println("=== Meta Commands ===");
        System.out.println("  SHOW TABLES        - List all tables");
        System.out.println("  DESCRIBE <table>   - Show table structure");
        System.out.println("  DESC <table>       - Alias for DESCRIBE");
        System.out.println("  help               - Show this help");
        System.out.println("  clear              - Clear screen");
        System.out.println("  exit               - Exit REPL");
        System.out.println();
    }

    private void showTables() {
        System.out.println("\n+------------------+");
        System.out.println("| Tables           |");
        System.out.println("+------------------+");
        
        if (schema.getAllTables().isEmpty()) {
            System.out.println("| (no tables)      |");
        } else {
            for (var table : schema.getAllTables()) {
                System.out.printf("| %-16s |\n", table.getName());
            }
        }
        System.out.println("+------------------+");
        System.out.println();
    }

    private void describeTable(String tableName) {
        var tableOpt = schema.getTable(tableName);
        if (!tableOpt.isPresent()) {
            System.out.println("✗ Table does not exist: " + tableName);
            System.out.println();
            return;
        }
        
        var table = tableOpt.get();
        System.out.println("\nTable: " + table.getName());
        System.out.println("+-------------+-------------+---------+--------+--------+");
        System.out.println("| Column      | Type        | Primary | Unique | Null   |");
        System.out.println("+-------------+-------------+---------+--------+--------+");
        
        for (var column : table.getColumns()) {
            String type = column.getDataType().name();
            if (column.getDataType().name().equals("VARCHAR")) {
                type = "VARCHAR(" + column.getSize() + ")";
            }
            System.out.printf("| %-11s | %-11s | %-7s | %-6s | %-6s |\n",
                truncate(column.getName(), 11),
                truncate(type, 11),
                column.isPrimaryKey() ? "YES" : "NO",
                column.isUnique() ? "YES" : "NO",
                column.isNullable() ? "YES" : "NO");
        }
        System.out.println("+-------------+-------------+---------+--------+--------+");
        System.out.println();
    }

    private String truncate(String str, int maxLen) {
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {
        DatabaseREPL repl = new DatabaseREPL();
        repl.start();
    }
}
