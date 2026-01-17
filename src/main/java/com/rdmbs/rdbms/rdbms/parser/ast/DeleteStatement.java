package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DeleteStatement extends Statement {
    private String tableName;
    private WhereClause whereClause;

    @Override
    public StatementType getType() {
        return StatementType.DELETE;
    }
}
