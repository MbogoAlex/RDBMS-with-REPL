package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class SelectStatement extends Statement {
    private List<String> columns;
    private String tableName;
    private WhereClause whereClause;
    private JoinClause joinClause;

    @Override
    public StatementType getType() {
        return StatementType.SELECT;
    }
}
