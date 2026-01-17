package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateTableStatement extends Statement {
    private String tableName;
    private List<ColumnDefinition> columns;

    @Override
    public StatementType getType() {
        return StatementType.CREATE_TABLE;
    }
}
