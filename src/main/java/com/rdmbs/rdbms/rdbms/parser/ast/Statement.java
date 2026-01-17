package com.rdmbs.rdbms.rdbms.parser.ast;

public abstract class Statement {
    public abstract StatementType getType();
    
    public enum StatementType {
        CREATE_TABLE,
        DROP_TABLE,
        INSERT,
        SELECT,
        UPDATE,
        DELETE,
        CREATE_INDEX
    }
}
