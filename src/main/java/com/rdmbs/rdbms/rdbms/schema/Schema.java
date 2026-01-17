package com.rdmbs.rdbms.rdbms.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Collection;

public class Schema {
    private final Map<String, Table> tables;

    public Schema() {
        this.tables = new HashMap<>();
    }

    public void addTable(Table table) {
        tables.put(table.getName().toLowerCase(), table);
    }

    public Optional<Table> getTable(String tableName) {
        return Optional.ofNullable(tables.get(tableName.toLowerCase()));
    }

    public boolean tableExists(String tableName) {
        return tables.containsKey(tableName.toLowerCase());
    }

    public void dropTable(String tableName) {
        tables.remove(tableName.toLowerCase());
    }

    public Collection<Table> getAllTables() {
        return tables.values();
    }
}
