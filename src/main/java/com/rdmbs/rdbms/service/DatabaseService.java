package com.rdmbs.rdbms.service;

import com.rdmbs.rdbms.dto.SQLResponse;
import com.rdmbs.rdbms.dto.TableInfo;
import com.rdmbs.rdbms.rdbms.engine.QueryEngine;
import com.rdmbs.rdbms.rdbms.engine.QueryResult;
import com.rdmbs.rdbms.rdbms.index.IndexManager;
import com.rdmbs.rdbms.rdbms.schema.Column;
import com.rdmbs.rdbms.rdbms.schema.Schema;
import com.rdmbs.rdbms.rdbms.schema.SchemaManager;
import com.rdmbs.rdbms.rdbms.schema.Table;
import com.rdmbs.rdbms.rdbms.storage.Row;
import com.rdmbs.rdbms.rdbms.storage.TableStorage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatabaseService {
    private final QueryEngine queryEngine;
    private final Schema schema;
    private final TableStorage storage;

    public DatabaseService() {
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
    }

    public SQLResponse executeSQL(String sql) {
        long startTime = System.currentTimeMillis();
        
        // Remove trailing semicolon
        sql = sql.replaceAll(";\\s*$", "").trim();
        
        // Handle meta commands
        if (sql.toUpperCase().equals("SHOW TABLES")) {
            return handleShowTables(startTime);
        }
        
        if (sql.toUpperCase().startsWith("DESCRIBE ") || sql.toUpperCase().startsWith("DESC ")) {
            String tableName = sql.split("\\s+")[1];
            return handleDescribe(tableName, startTime);
        }
        
        // Execute regular SQL
        QueryResult result = queryEngine.execute(sql);
        
        long endTime = System.currentTimeMillis();
        
        SQLResponse response = new SQLResponse();
        response.setSuccess(result.isSuccess());
        response.setMessage(result.getMessage());
        response.setExecutionTimeMs(endTime - startTime);
        
        if (result.isSuccess() && result.getRows() != null && !result.getRows().isEmpty()) {
            response.setColumnNames(result.getColumnNames());
            response.setRows(convertRowsToMaps(result.getColumnNames(), result.getRows()));
            response.setRowCount(result.getRows().size());
        } else {
            response.setRowCount(result.getRowsAffected());
        }
        
        return response;
    }

    private SQLResponse handleShowTables(long startTime) {
        List<String> tables = getAllTableNames();
        
        SQLResponse response = new SQLResponse();
        response.setSuccess(true);
        response.setMessage("Tables retrieved");
        response.setColumnNames(List.of("Tables"));
        
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String table : tables) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Tables", table);
            rows.add(row);
        }
        
        response.setRows(rows);
        response.setRowCount(tables.size());
        response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        
        return response;
    }

    private SQLResponse handleDescribe(String tableName, long startTime) {
        try {
            TableInfo info = getTableInfo(tableName);
            if (info == null) {
                SQLResponse response = new SQLResponse();
                response.setSuccess(false);
                response.setMessage("Table does not exist: " + tableName);
                response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return response;
            }
            
            SQLResponse response = new SQLResponse();
            response.setSuccess(true);
            response.setMessage("Table structure for: " + tableName);
            response.setColumnNames(List.of("Column", "Type", "Primary", "Unique", "Nullable"));
            
            List<Map<String, Object>> rows = new ArrayList<>();
            for (TableInfo.ColumnInfo col : info.getColumns()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("Column", col.getName());
                row.put("Type", col.getDataType());
                row.put("Primary", col.isPrimaryKey() ? "YES" : "NO");
                row.put("Unique", col.isUnique() ? "YES" : "NO");
                row.put("Nullable", col.isNullable() ? "YES" : "NO");
                rows.add(row);
            }
            
            response.setRows(rows);
            response.setRowCount(rows.size());
            response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            
            return response;
        } catch (Exception e) {
            SQLResponse response = new SQLResponse();
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    public List<String> getAllTableNames() {
        return schema.getAllTables().stream()
                .map(Table::getName)
                .collect(Collectors.toList());
    }

    public TableInfo getTableInfo(String tableName) throws IOException {
        Optional<Table> tableOpt = schema.getTable(tableName);
        if (!tableOpt.isPresent()) {
            return null;
        }
        
        Table table = tableOpt.get();
        TableInfo info = new TableInfo();
        info.setTableName(table.getName());
        
        List<TableInfo.ColumnInfo> columns = new ArrayList<>();
        for (Column col : table.getColumns()) {
            TableInfo.ColumnInfo colInfo = new TableInfo.ColumnInfo();
            colInfo.setName(col.getName());
            colInfo.setDataType(col.getDataType().name());
            colInfo.setPrimaryKey(col.isPrimaryKey());
            colInfo.setUnique(col.isUnique());
            colInfo.setNullable(col.isNullable());
            columns.add(colInfo);
        }
        info.setColumns(columns);
        
        List<Row> rows = storage.readAllRows(table);
        info.setRowCount(rows.size());
        
        return info;
    }

    public void initializeDemoData() {
        String[] demoSQL = {
            "CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100), price INT, stock INT, category VARCHAR(50))",
            "INSERT INTO products VALUES (1, 'Laptop', 75000, 10, 'Electronics')",
            "INSERT INTO products VALUES (2, 'Mouse', 1500, 50, 'Electronics')",
            "INSERT INTO products VALUES (3, 'Keyboard', 3000, 30, 'Electronics')",
            "INSERT INTO products VALUES (4, 'Monitor', 25000, 15, 'Electronics')",
            "INSERT INTO products VALUES (5, 'Desk Chair', 12000, 20, 'Furniture')",
            "CREATE TABLE sales (id INT PRIMARY KEY, product_id INT, quantity INT, total_amount INT, sale_date VARCHAR(20))"
        };
        
        for (String sql : demoSQL) {
            queryEngine.execute(sql);
        }
    }

    private List<Map<String, Object>> convertRowsToMaps(List<String> columnNames, List<Row> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Row row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < columnNames.size() && i < row.size(); i++) {
                map.put(columnNames.get(i), row.getValue(i));
            }
            result.add(map);
        }
        
        return result;
    }
}
