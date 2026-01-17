package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateIndexStatement extends Statement {
    private String indexName;
    private String tableName;
    private String columnName;
    private boolean unique;

    @Override
    public StatementType getType() {
        return StatementType.CREATE_INDEX;
    }
}
