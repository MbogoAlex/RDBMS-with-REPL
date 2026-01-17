package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ColumnDefinition {
    private String name;
    private String dataType;
    private Integer size;
    private boolean primaryKey;
    private boolean unique;
    private boolean notNull;
}
