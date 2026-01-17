package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class InsertStatement extends Statement {
    private String tableName;
    private List<String> columns;
    private List<Object> values;

    @Override
    public StatementType getType() {
        return StatementType.INSERT;
    }
}
