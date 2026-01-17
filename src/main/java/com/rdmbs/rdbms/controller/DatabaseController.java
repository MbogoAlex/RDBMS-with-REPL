package com.rdmbs.rdbms.controller;

import com.rdmbs.rdbms.dto.SQLRequest;
import com.rdmbs.rdbms.dto.SQLResponse;
import com.rdmbs.rdbms.dto.TableInfo;
import com.rdmbs.rdbms.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    @PostMapping("/execute")
    public ResponseEntity<SQLResponse> executeSQL(@RequestBody SQLRequest request) {
        if (request.getSql() == null || request.getSql().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(SQLResponse.error("SQL query cannot be empty"));
        }
        
        SQLResponse response = databaseService.executeSQL(request.getSql());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tables")
    public ResponseEntity<List<String>> getAllTables() {
        List<String> tables = databaseService.getAllTableNames();
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/tables/{name}/schema")
    public ResponseEntity<TableInfo> getTableSchema(@PathVariable String name) throws IOException {
        TableInfo info = databaseService.getTableInfo(name);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @PostMapping("/init-demo-data")
    public ResponseEntity<Map<String, String>> initDemoData() {
        databaseService.initializeDemoData();
        return ResponseEntity.ok(Map.of("message", "Demo data initialized successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "database", "Duka RDBMS"));
    }
}
