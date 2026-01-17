package com.rdmbs.rdbms.rdbms.engine;

import com.rdmbs.rdbms.rdbms.storage.Row;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QueryResult {
    private boolean success;
    private String message;
    private List<String> columnNames;
    private List<Row> rows;
    private int rowsAffected;

    public QueryResult() {
        this.rows = new ArrayList<>();
        this.columnNames = new ArrayList<>();
    }

    public static QueryResult success(String message) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    public static QueryResult error(String message) {
        QueryResult result = new QueryResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public static QueryResult withRows(List<String> columnNames, List<Row> rows) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);
        result.setColumnNames(columnNames);
        result.setRows(rows);
        result.setRowsAffected(rows.size());
        return result;
    }
}
