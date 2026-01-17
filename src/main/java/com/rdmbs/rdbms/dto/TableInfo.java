package com.rdmbs.rdbms.dto;

import lombok.Data;
import java.util.List;

@Data
public class TableInfo {
    private String tableName;
    private List<ColumnInfo> columns;
    private int rowCount;

    @Data
    public static class ColumnInfo {
        private String name;
        private String dataType;
        private boolean primaryKey;
        private boolean unique;
        private boolean nullable;
    }
}
