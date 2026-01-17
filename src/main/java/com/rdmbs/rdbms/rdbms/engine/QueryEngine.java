package com.rdmbs.rdbms.rdbms.engine;

import com.rdmbs.rdbms.rdbms.parser.SQLLexer;
import com.rdmbs.rdbms.rdbms.parser.SQLParser;
import com.rdmbs.rdbms.rdbms.parser.Token;
import com.rdmbs.rdbms.rdbms.parser.ast.*;
import com.rdmbs.rdbms.rdbms.schema.*;
import com.rdmbs.rdbms.rdbms.storage.Row;
import com.rdmbs.rdbms.rdbms.storage.TableStorage;
import com.rdmbs.rdbms.rdbms.index.IndexManager;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class QueryEngine {
    private final Schema schema;
    private final TableStorage storage;
    private final IndexManager indexManager;
    private final SchemaManager schemaManager;

    public QueryEngine(Schema schema, TableStorage storage, IndexManager indexManager) {
        this.schema = schema;
        this.storage = storage;
        this.indexManager = indexManager;
        this.schemaManager = new SchemaManager();
    }

    public QueryResult execute(String sql) {
        try {
            SQLLexer lexer = new SQLLexer(sql);
            List<Token> tokens = lexer.tokenize();
            
            SQLParser parser = new SQLParser(tokens);
            Statement statement = parser.parse();
            
            return executeStatement(statement);
        } catch (Exception e) {
            return QueryResult.error("Error: " + e.getMessage());
        }
    }

    private QueryResult executeStatement(Statement statement) {
        try {
            switch (statement.getType()) {
                case CREATE_TABLE:
                    return executeCreateTable((CreateTableStatement) statement);
                case DROP_TABLE:
                    return executeDropTable((DropTableStatement) statement);
                case INSERT:
                    return executeInsert((InsertStatement) statement);
                case SELECT:
                    return executeSelect((SelectStatement) statement);
                case UPDATE:
                    return executeUpdate((UpdateStatement) statement);
                case DELETE:
                    return executeDelete((DeleteStatement) statement);
                case CREATE_INDEX:
                    return executeCreateIndex((CreateIndexStatement) statement);
                default:
                    return QueryResult.error("Unsupported statement type");
            }
        } catch (Exception e) {
            return QueryResult.error("Execution error: " + e.getMessage());
        }
    }

    private QueryResult executeCreateTable(CreateTableStatement stmt) throws IOException {
        if (schema.tableExists(stmt.getTableName())) {
            return QueryResult.error("Table already exists: " + stmt.getTableName());
        }

        Table table = new Table(stmt.getTableName());
        
        for (ColumnDefinition colDef : stmt.getColumns()) {
            DataType dataType = DataType.fromString(colDef.getDataType());
            Column column;
            
            if (colDef.getSize() != null) {
                column = new Column(colDef.getName(), dataType, colDef.getSize());
            } else {
                column = new Column(colDef.getName(), dataType);
            }
            
            column.setPrimaryKey(colDef.isPrimaryKey());
            column.setUnique(colDef.isUnique());
            column.setNullable(!colDef.isNotNull());
            
            table.addColumn(column);
        }
        
        schema.addTable(table);
        storage.createTableFile(table);
        
        // Persist schema
        try {
            schemaManager.saveSchema(schema);
        } catch (IOException e) {

        }
        
        return QueryResult.success("Table created: " + stmt.getTableName());
    }

    private QueryResult executeDropTable(DropTableStatement stmt) throws IOException {
        if (!schema.tableExists(stmt.getTableName())) {
            return QueryResult.error("Table does not exist: " + stmt.getTableName());
        }
        
        schema.dropTable(stmt.getTableName());
        storage.deleteTable(stmt.getTableName());
        indexManager.dropTableIndexes(stmt.getTableName());
        
        // Persist schema
        try {
            schemaManager.saveSchema(schema);
        } catch (IOException e) {
        }
        
        return QueryResult.success("Table dropped: " + stmt.getTableName());
    }

    private QueryResult executeInsert(InsertStatement stmt) throws IOException {
        Optional<Table> tableOpt = schema.getTable(stmt.getTableName());
        if (!tableOpt.isPresent()) {
            return QueryResult.error("Table does not exist: " + stmt.getTableName());
        }
        
        Table table = tableOpt.get();
        Row row = new Row();
        
        if (stmt.getColumns() != null && !stmt.getColumns().isEmpty()) {
            for (Column column : table.getColumns()) {
                int valueIndex = stmt.getColumns().indexOf(column.getName());
                if (valueIndex >= 0) {
                    Object value = convertValue(stmt.getValues().get(valueIndex), column.getDataType());
                    row.addValue(value);
                } else {
                    row.addValue(null);
                }
            }
        } else {
            for (int i = 0; i < stmt.getValues().size(); i++) {
                Object value = convertValue(stmt.getValues().get(i), table.getColumns().get(i).getDataType());
                row.addValue(value);
            }
        }
        
        if (!validateConstraints(table, row)) {
            return QueryResult.error("Constraint violation");
        }
        
        storage.insertRow(table, row);
        indexManager.insertIntoIndexes(table, row);
        
        QueryResult result = QueryResult.success("1 row inserted");
        result.setRowsAffected(1);
        return result;
    }

    private QueryResult executeSelect(SelectStatement stmt) throws IOException {
        Optional<Table> tableOpt = schema.getTable(stmt.getTableName());
        if (!tableOpt.isPresent()) {
            return QueryResult.error("Table does not exist: " + stmt.getTableName());
        }
        
        Table table = tableOpt.get();
        List<Row> rows = storage.readAllRows(table);
        
        if (stmt.getWhereClause() != null) {
            rows = filterRows(table, rows, stmt.getWhereClause());
        }
        
        if (stmt.getJoinClause() != null) {
            rows = performJoin(table, rows, stmt.getJoinClause());
        }
        
        List<String> selectedColumns = stmt.getColumns();
        if (selectedColumns.contains("*")) {
            selectedColumns = table.getColumns().stream()
                    .map(Column::getName)
                    .collect(Collectors.toList());
        }
        
        rows = projectColumns(table, rows, selectedColumns);
        
        return QueryResult.withRows(selectedColumns, rows);
    }

    private QueryResult executeUpdate(UpdateStatement stmt) throws IOException {
        Optional<Table> tableOpt = schema.getTable(stmt.getTableName());
        if (!tableOpt.isPresent()) {
            return QueryResult.error("Table does not exist: " + stmt.getTableName());
        }
        
        Table table = tableOpt.get();
        List<Row> rows = storage.readAllRows(table);
        List<Row> filteredRows = rows;
        
        if (stmt.getWhereClause() != null) {
            filteredRows = filterRows(table, rows, stmt.getWhereClause());
        }
        
        int updateCount = 0;
        for (Row row : filteredRows) {
            for (Map.Entry<String, Object> entry : stmt.getUpdates().entrySet()) {
                int columnIndex = table.getColumnIndex(entry.getKey());
                if (columnIndex >= 0) {
                    Object value = convertValue(entry.getValue(), table.getColumns().get(columnIndex).getDataType());
                    row.setValue(columnIndex, value);
                    updateCount++;
                }
            }
        }
        
        storage.truncateTable(stmt.getTableName());
        for (Row row : rows) {
            storage.insertRow(table, row);
        }
        
        QueryResult result = QueryResult.success(updateCount + " row(s) updated");
        result.setRowsAffected(updateCount);
        return result;
    }

    private QueryResult executeDelete(DeleteStatement stmt) throws IOException {
        Optional<Table> tableOpt = schema.getTable(stmt.getTableName());
        if (!tableOpt.isPresent()) {
            return QueryResult.error("Table does not exist: " + stmt.getTableName());
        }
        
        Table table = tableOpt.get();
        List<Row> rows = storage.readAllRows(table);
        int originalSize = rows.size();
        
        if (stmt.getWhereClause() != null) {
            List<Row> toDelete = filterRows(table, rows, stmt.getWhereClause());
            rows.removeAll(toDelete);
        } else {
            rows.clear();
        }
        
        int deletedCount = originalSize - rows.size();
        
        storage.truncateTable(stmt.getTableName());
        for (Row row : rows) {
            storage.insertRow(table, row);
        }
        
        QueryResult result = QueryResult.success(deletedCount + " row(s) deleted");
        result.setRowsAffected(deletedCount);
        return result;
    }

    private QueryResult executeCreateIndex(CreateIndexStatement stmt) throws IOException {
        Optional<Table> tableOpt = schema.getTable(stmt.getTableName());
        if (!tableOpt.isPresent()) {
            return QueryResult.error("Table does not exist: " + stmt.getTableName());
        }
        
        Table table = tableOpt.get();
        indexManager.createIndex(stmt.getIndexName(), table, stmt.getColumnName(), stmt.isUnique());
        
        return QueryResult.success("Index created: " + stmt.getIndexName());
    }

    private List<Row> filterRows(Table table, List<Row> rows, WhereClause where) {
        return rows.stream()
                .filter(row -> evaluateCondition(table, row, where))
                .collect(Collectors.toList());
    }

    private boolean evaluateCondition(Table table, Row row, WhereClause condition) {
        int leftIndex = table.getColumnIndex(condition.getLeftColumn());
        if (leftIndex < 0) return false;
        
        Object leftValue = row.getValue(leftIndex);
        Object rightValue;
        
        if (condition.isColumnComparison()) {
            int rightIndex = table.getColumnIndex(condition.getRightColumn());
            if (rightIndex < 0) return false;
            rightValue = row.getValue(rightIndex);
        } else {
            rightValue = condition.getRightValue();
        }
        
        boolean result = compareValues(leftValue, rightValue, condition.getOperator());
        
        if (condition.getNextCondition() != null) {
            boolean nextResult = evaluateCondition(table, row, condition.getNextCondition());
            if (condition.getLogicalOperator() == WhereClause.LogicalOperator.AND) {
                result = result && nextResult;
            } else {
                result = result || nextResult;
            }
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean compareValues(Object left, Object right, WhereClause.Operator op) {
        if (left == null || right == null) {
            return op == WhereClause.Operator.EQUALS ? (left == right) : (left != right);
        }
        
        if (left instanceof Comparable && right instanceof Comparable) {
            int comparison = ((Comparable) left).compareTo(right);
            
            switch (op) {
                case EQUALS: return comparison == 0;
                case NOT_EQUALS: return comparison != 0;
                case LESS_THAN: return comparison < 0;
                case GREATER_THAN: return comparison > 0;
                case LESS_EQUAL: return comparison <= 0;
                case GREATER_EQUAL: return comparison >= 0;
            }
        }
        
        return false;
    }

    private List<Row> projectColumns(Table table, List<Row> rows, List<String> columns) {
        List<Integer> columnIndexes = columns.stream()
                .map(table::getColumnIndex)
                .collect(Collectors.toList());
        
        return rows.stream()
                .map(row -> {
                    Row projected = new Row();
                    for (int index : columnIndexes) {
                        if (index >= 0) {
                            projected.addValue(row.getValue(index));
                        }
                    }
                    return projected;
                })
                .collect(Collectors.toList());
    }

    private List<Row> performJoin(Table leftTable, List<Row> leftRows, JoinClause join) throws IOException {
        Optional<Table> rightTableOpt = schema.getTable(join.getRightTable());
        if (!rightTableOpt.isPresent()) {
            throw new RuntimeException("Table does not exist: " + join.getRightTable());
        }
        
        Table rightTable = rightTableOpt.get();
        List<Row> rightRows = storage.readAllRows(rightTable);
        
        int leftColIndex = leftTable.getColumnIndex(join.getLeftColumn());
        int rightColIndex = rightTable.getColumnIndex(join.getRightColumn());
        
        List<Row> result = new ArrayList<>();
        
        for (Row leftRow : leftRows) {
            Object leftValue = leftRow.getValue(leftColIndex);
            boolean matched = false;
            
            for (Row rightRow : rightRows) {
                Object rightValue = rightRow.getValue(rightColIndex);
                
                if (Objects.equals(leftValue, rightValue)) {
                    Row joined = new Row();
                    for (Object val : leftRow.getValues()) {
                        joined.addValue(val);
                    }
                    for (Object val : rightRow.getValues()) {
                        joined.addValue(val);
                    }
                    result.add(joined);
                    matched = true;
                }
            }
            
            if (!matched && join.getJoinType() == JoinClause.JoinType.LEFT) {
                Row joined = new Row();
                for (Object val : leftRow.getValues()) {
                    joined.addValue(val);
                }
                for (int i = 0; i < rightTable.getColumns().size(); i++) {
                    joined.addValue(null);
                }
                result.add(joined);
            }
        }
        
        return result;
    }

    private Object convertValue(Object value, DataType targetType) {
        if (value == null) return null;
        
        switch (targetType) {
            case INT:
                if (value instanceof Integer) return value;
                return Integer.parseInt(value.toString());
            case LONG:
            case DATE:
            case DATETIME:
            case TIMESTAMP:
                if (value instanceof Long) return value;
                return Long.parseLong(value.toString());
            case BOOLEAN:
                if (value instanceof Boolean) return value;
                return Boolean.parseBoolean(value.toString());
            case VARCHAR:
                return value.toString();
            default:
                return value;
        }
    }

    private boolean validateConstraints(Table table, Row row) throws IOException {
        for (int i = 0; i < table.getColumns().size(); i++) {
            Column column = table.getColumns().get(i);
            Object value = row.getValue(i);
            
            if (!column.isNullable() && value == null) {
                return false;
            }
            
            if (column.isPrimaryKey() || column.isUnique()) {
                List<Row> existingRows = storage.readAllRows(table);
                for (Row existing : existingRows) {
                    if (Objects.equals(existing.getValue(i), value)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
