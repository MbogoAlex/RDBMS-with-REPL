package com.rdmbs.rdbms.rdbms.index;

import com.rdmbs.rdbms.rdbms.storage.Row;
import lombok.Getter;

import java.util.*;

@Getter
public class BTreeIndex implements Index {
    private final String indexName;
    private final String tableName;
    private final String columnName;
    private final int columnIndex;
    private final boolean unique;
    private final TreeMap<Comparable, List<Row>> index;

    public BTreeIndex(String indexName, String tableName, String columnName, int columnIndex, boolean unique) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.unique = unique;
        this.index = new TreeMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void insert(Row row) {
        Object keyValue = row.getValue(columnIndex);
        if (keyValue == null) return;
        
        if (!(keyValue instanceof Comparable)) {
            throw new RuntimeException("Index key must be comparable");
        }
        
        Comparable key = (Comparable) keyValue;
        
        if (unique && index.containsKey(key)) {
            throw new RuntimeException("Unique constraint violation on index: " + indexName);
        }
        
        index.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Row> search(Object keyValue) {
        if (keyValue == null || !(keyValue instanceof Comparable)) {
            return new ArrayList<>();
        }
        
        Comparable key = (Comparable) keyValue;
        return index.getOrDefault(key, new ArrayList<>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void delete(Object keyValue) {
        if (keyValue instanceof Comparable) {
            index.remove((Comparable) keyValue);
        }
    }

    public List<Row> range(Comparable from, Comparable to) {
        List<Row> result = new ArrayList<>();
        for (List<Row> rows : index.subMap(from, true, to, true).values()) {
            result.addAll(rows);
        }
        return result;
    }

    public int size() {
        return index.values().stream().mapToInt(List::size).sum();
    }
}
