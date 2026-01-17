package com.rdmbs.rdbms.rdbms.index;

import com.rdmbs.rdbms.rdbms.schema.Table;
import com.rdmbs.rdbms.rdbms.storage.Row;

import java.io.IOException;
import java.util.*;

public class IndexManager {
    private final Map<String, Index> indexes;

    public IndexManager() {
        this.indexes = new HashMap<>();
    }

    public void createIndex(String indexName, Table table, String columnName, boolean unique) throws IOException {
        if (indexes.containsKey(indexName)) {
            throw new RuntimeException("Index already exists: " + indexName);
        }

        int columnIndex = table.getColumnIndex(columnName);
        if (columnIndex < 0) {
            throw new RuntimeException("Column not found: " + columnName);
        }

        BTreeIndex index = new BTreeIndex(indexName, table.getName(), columnName, columnIndex, unique);
        indexes.put(indexName, index);
    }

    public void insertIntoIndexes(Table table, Row row) {
        for (Index index : indexes.values()) {
            if (index.getTableName().equals(table.getName())) {
                index.insert(row);
            }
        }
    }

    public List<Row> search(String indexName, Object key) {
        Index index = indexes.get(indexName);
        if (index == null) {
            return new ArrayList<>();
        }
        return index.search(key);
    }

    public void dropTableIndexes(String tableName) {
        indexes.entrySet().removeIf(entry -> entry.getValue().getTableName().equals(tableName));
    }

    public void dropIndex(String indexName) {
        indexes.remove(indexName);
    }

    public boolean hasIndex(String tableName, String columnName) {
        return indexes.values().stream()
                .anyMatch(idx -> idx.getTableName().equals(tableName) && idx.getColumnName().equals(columnName));
    }

    public Collection<Index> getAllIndexes() {
        return indexes.values();
    }
}
