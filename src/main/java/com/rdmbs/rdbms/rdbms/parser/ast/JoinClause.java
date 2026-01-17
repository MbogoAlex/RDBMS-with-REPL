package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;

@Data
public class JoinClause {
    private JoinType joinType;
    private String rightTable;
    private String leftColumn;
    private String rightColumn;

    public enum JoinType {
        INNER, LEFT, RIGHT
    }
}
