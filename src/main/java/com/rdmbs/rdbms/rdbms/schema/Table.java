package com.rdmbs.rdbms.rdbms.schema;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Table {
    private String name;
    private List<Column> columns;
    private Column primaryKey;

    public Table(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
    }

    public void addColumn(Column column) {
        columns.add(column);
        if (column.isPrimaryKey()) {
            this.primaryKey = column;
        }
    }

    public Optional<Column> getColumn(String columnName) {
        return columns.stream()
                .filter(col -> col.getName().equalsIgnoreCase(columnName))
                .findFirst();
    }

    public int getColumnIndex(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    public int getRowSize() {
        return columns.stream()
                .mapToInt(Column::getByteSize)
                .sum();
    }
}
