package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DropTableStatement extends Statement {
    private String tableName;

    @Override
    public StatementType getType() {
        return StatementType.DROP_TABLE;
    }
}
