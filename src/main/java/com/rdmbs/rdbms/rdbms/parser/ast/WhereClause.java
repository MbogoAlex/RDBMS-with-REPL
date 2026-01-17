package com.rdmbs.rdbms.rdbms.parser.ast;

import lombok.Data;

@Data
public class WhereClause {
    private String leftColumn;
    private Operator operator;
    private Object rightValue;
    private String rightColumn;
    private LogicalOperator logicalOperator;
    private WhereClause nextCondition;

    public enum Operator {
        EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_EQUAL, GREATER_EQUAL
    }

    public enum LogicalOperator {
        AND, OR
    }

    public boolean isColumnComparison() {
        return rightColumn != null;
    }
}
