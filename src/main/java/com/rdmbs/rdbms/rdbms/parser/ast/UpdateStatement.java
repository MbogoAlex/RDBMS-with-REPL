package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateStatement extends Statement {
    private String tableName;
    private Map<String, Object> updates;
    private WhereClause whereClause;

    @Override
    public StatementType getType() {
        return StatementType.UPDATE;
    }
}
