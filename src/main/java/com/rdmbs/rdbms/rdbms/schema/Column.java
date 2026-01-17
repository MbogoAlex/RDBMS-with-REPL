package com.rdmbs.rdbms.rdbms.schema;

import lombok.Data;

@Data
public class Column {
    private String name;
    private DataType dataType;
    private int size;
    private boolean nullable;
    private boolean primaryKey;
    private boolean unique;
    private Object defaultValue;

    public Column(String name, DataType dataType) {
        this.name = name;
        this.dataType = dataType;
        this.size = dataType.getDefaultSize();
        this.nullable = true;
        this.primaryKey = false;
        this.unique = false;
    }

    public Column(String name, DataType dataType, int size) {
        this(name, dataType);
        this.size = size;
    }

    public int getByteSize() {
        return dataType == DataType.VARCHAR ? size : dataType.getDefaultSize();
    }
}
