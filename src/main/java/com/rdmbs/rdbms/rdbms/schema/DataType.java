package com.rdmbs.rdbms.rdbms.schema;

public enum DataType {
    INT(4),
    LONG(8),
    VARCHAR(255),
    BOOLEAN(1),
    DATE(8),        // milliseconds since epoch
    DATETIME(8),    // milliseconds since epoch
    TIMESTAMP(8);   // milliseconds since epoch

    private final int defaultSize;

    DataType(int defaultSize) {
        this.defaultSize = defaultSize;
    }

    public int getDefaultSize() {
        return defaultSize;
    }

    public static DataType fromString(String type) {
        String upperType = type.toUpperCase();
        if (upperType.startsWith("VARCHAR")) {
            return VARCHAR;
        }
        return valueOf(upperType);
    }

    public boolean isDateType() {
        return this == DATE || this == DATETIME || this == TIMESTAMP;
    }
}
