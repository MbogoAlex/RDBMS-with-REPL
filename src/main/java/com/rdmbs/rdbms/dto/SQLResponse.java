package com.rdmbs.rdbms.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SQLResponse {
    private boolean success;
    private String message;
    private List<String> columnNames;
    private List<Map<String, Object>> rows;
    private int rowCount;
    private long executionTimeMs;

    public static SQLResponse success(String message) {
        SQLResponse response = new SQLResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static SQLResponse error(String message) {
        SQLResponse response = new SQLResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
