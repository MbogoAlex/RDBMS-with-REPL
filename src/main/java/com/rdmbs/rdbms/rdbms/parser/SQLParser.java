package com.rdmbs.rdbms.rdbms.parser;

import com.rdmbs.rdbms.rdbms.parser.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLParser {
    private final List<Token> tokens;
    private int position;
    private Token currentToken;

    public SQLParser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.currentToken = tokens.isEmpty() ? null : tokens.get(0);
    }

    public Statement parse() {
        if (currentToken == null) {
            throw new RuntimeException("Empty SQL statement");
        }

        switch (currentToken.getType()) {
            case CREATE:
                return parseCreate();
            case DROP:
                return parseDrop();
            case INSERT:
                return parseInsert();
            case SELECT:
                return parseSelect();
            case UPDATE:
                return parseUpdate();
            case DELETE:
                return parseDelete();
            default:
                throw new RuntimeException("Unexpected token: " + currentToken.getValue());
        }
    }

    private Statement parseCreate() {
        consume(TokenType.CREATE);
        
        if (match(TokenType.TABLE)) {
            return parseCreateTable();
        } else if (match(TokenType.INDEX) || match(TokenType.UNIQUE)) {
            return parseCreateIndex();
        }
        
        throw new RuntimeException("Expected TABLE or INDEX after CREATE");
    }

    private CreateTableStatement parseCreateTable() {
        consume(TokenType.TABLE);
        
        CreateTableStatement stmt = new CreateTableStatement();
        stmt.setTableName(consume(TokenType.IDENTIFIER).getValue());
        
        consume(TokenType.LEFT_PAREN);
        stmt.setColumns(parseColumnDefinitions());
        consume(TokenType.RIGHT_PAREN);
        
        return stmt;
    }

    private List<ColumnDefinition> parseColumnDefinitions() {
        List<ColumnDefinition> columns = new ArrayList<>();
        
        do {
            if (match(TokenType.COMMA)) {
                consume(TokenType.COMMA);
            }
            
            ColumnDefinition col = new ColumnDefinition();
            col.setName(consume(TokenType.IDENTIFIER).getValue());
            
            String dataType = consume(TokenType.IDENTIFIER).getValue();
            
            if (match(TokenType.LEFT_PAREN)) {
                consume(TokenType.LEFT_PAREN);
                col.setSize(Integer.parseInt(consume(TokenType.NUMBER).getValue()));
                consume(TokenType.RIGHT_PAREN);
            }
            
            col.setDataType(dataType);
            
            while (!match(TokenType.COMMA) && !match(TokenType.RIGHT_PAREN)) {
                if (match(TokenType.PRIMARY)) {
                    consume(TokenType.PRIMARY);
                    consume(TokenType.KEY);
                    col.setPrimaryKey(true);
                } else if (match(TokenType.UNIQUE)) {
                    consume(TokenType.UNIQUE);
                    col.setUnique(true);
                } else if (match(TokenType.NOT)) {
                    consume(TokenType.NOT);
                    consume(TokenType.NULL);
                    col.setNotNull(true);
                } else {
                    break;
                }
            }
            
            columns.add(col);
            
        } while (match(TokenType.COMMA));
        
        return columns;
    }

    private CreateIndexStatement parseCreateIndex() {
        boolean unique = false;
        if (match(TokenType.UNIQUE)) {
            consume(TokenType.UNIQUE);
            unique = true;
        }
        
        consume(TokenType.INDEX);
        
        CreateIndexStatement stmt = new CreateIndexStatement();
        stmt.setIndexName(consume(TokenType.IDENTIFIER).getValue());
        stmt.setUnique(unique);
        
        consume(TokenType.ON);
        stmt.setTableName(consume(TokenType.IDENTIFIER).getValue());
        
        consume(TokenType.LEFT_PAREN);
        stmt.setColumnName(consume(TokenType.IDENTIFIER).getValue());
        consume(TokenType.RIGHT_PAREN);
        
        return stmt;
    }

    private DropTableStatement parseDrop() {
        consume(TokenType.DROP);
        consume(TokenType.TABLE);
        
        DropTableStatement stmt = new DropTableStatement();
        stmt.setTableName(consume(TokenType.IDENTIFIER).getValue());
        
        return stmt;
    }

    private InsertStatement parseInsert() {
        consume(TokenType.INSERT);
        consume(TokenType.INTO);
        
        InsertStatement stmt = new InsertStatement();
        stmt.setTableName(consume(TokenType.IDENTIFIER).getValue());
        
        if (match(TokenType.LEFT_PAREN)) {
            consume(TokenType.LEFT_PAREN);
            stmt.setColumns(parseIdentifierList());
            consume(TokenType.RIGHT_PAREN);
        }
        
        consume(TokenType.VALUES);
        consume(TokenType.LEFT_PAREN);
        stmt.setValues(parseValueList());
        consume(TokenType.RIGHT_PAREN);
        
        return stmt;
    }

    private SelectStatement parseSelect() {
        consume(TokenType.SELECT);
        
        SelectStatement stmt = new SelectStatement();
        
        if (match(TokenType.ASTERISK)) {
            consume(TokenType.ASTERISK);
            stmt.setColumns(List.of("*"));
        } else {
            stmt.setColumns(parseIdentifierList());
        }
        
        consume(TokenType.FROM);
        stmt.setTableName(consume(TokenType.IDENTIFIER).getValue());
        
        if (match(TokenType.INNER) || match(TokenType.LEFT) || match(TokenType.RIGHT) || match(TokenType.JOIN)) {
            stmt.setJoinClause(parseJoin());
        }
        
        if (match(TokenType.WHERE)) {
            stmt.setWhereClause(parseWhere());
        }
        
        return stmt;
    }

    private UpdateStatement parseUpdate() {
        consume(TokenType.UPDATE);
        
        UpdateStatement stmt = new UpdateStatement();
        stmt.setTableName(consume(TokenType.IDENTIFIER).getValue());
        
        consume(TokenType.SET);
        stmt.setUpdates(parseUpdateAssignments());
        
        if (match(TokenType.WHERE)) {
            stmt.setWhereClause(parseWhere());
        }
        
        return stmt;
    }

    private DeleteStatement parseDelete() {
        consume(TokenType.DELETE);
        consume(TokenType.FROM);
        
        DeleteStatement stmt = new DeleteStatement();
        stmt.setTableName(consume(TokenType.IDENTIFIER).getValue());
        
        if (match(TokenType.WHERE)) {
            stmt.setWhereClause(parseWhere());
        }
        
        return stmt;
    }

    private WhereClause parseWhere() {
        consume(TokenType.WHERE);
        
        WhereClause clause = new WhereClause();
        clause.setLeftColumn(consume(TokenType.IDENTIFIER).getValue());
        clause.setOperator(parseOperator());
        
        if (match(TokenType.IDENTIFIER)) {
            clause.setRightColumn(consume(TokenType.IDENTIFIER).getValue());
        } else {
            clause.setRightValue(parseValue());
        }
        
        if (match(TokenType.AND) || match(TokenType.OR)) {
            if (match(TokenType.AND)) {
                consume(TokenType.AND);
                clause.setLogicalOperator(WhereClause.LogicalOperator.AND);
            } else {
                consume(TokenType.OR);
                clause.setLogicalOperator(WhereClause.LogicalOperator.OR);
            }
            clause.setNextCondition(parseWhereCondition());
        }
        
        return clause;
    }

    private WhereClause parseWhereCondition() {
        WhereClause clause = new WhereClause();
        clause.setLeftColumn(consume(TokenType.IDENTIFIER).getValue());
        clause.setOperator(parseOperator());
        
        if (match(TokenType.IDENTIFIER)) {
            clause.setRightColumn(consume(TokenType.IDENTIFIER).getValue());
        } else {
            clause.setRightValue(parseValue());
        }
        
        if (match(TokenType.AND) || match(TokenType.OR)) {
            if (match(TokenType.AND)) {
                consume(TokenType.AND);
                clause.setLogicalOperator(WhereClause.LogicalOperator.AND);
            } else {
                consume(TokenType.OR);
                clause.setLogicalOperator(WhereClause.LogicalOperator.OR);
            }
            clause.setNextCondition(parseWhereCondition());
        }
        
        return clause;
    }

    private JoinClause parseJoin() {
        JoinClause join = new JoinClause();
        
        if (match(TokenType.INNER)) {
            consume(TokenType.INNER);
            join.setJoinType(JoinClause.JoinType.INNER);
        } else if (match(TokenType.LEFT)) {
            consume(TokenType.LEFT);
            join.setJoinType(JoinClause.JoinType.LEFT);
        } else if (match(TokenType.RIGHT)) {
            consume(TokenType.RIGHT);
            join.setJoinType(JoinClause.JoinType.RIGHT);
        } else {
            join.setJoinType(JoinClause.JoinType.INNER);
        }
        
        consume(TokenType.JOIN);
        join.setRightTable(consume(TokenType.IDENTIFIER).getValue());
        
        consume(TokenType.ON);
        join.setLeftColumn(consume(TokenType.IDENTIFIER).getValue());
        consume(TokenType.EQUALS);
        join.setRightColumn(consume(TokenType.IDENTIFIER).getValue());
        
        return join;
    }

    private WhereClause.Operator parseOperator() {
        if (match(TokenType.EQUALS)) {
            consume(TokenType.EQUALS);
            return WhereClause.Operator.EQUALS;
        } else if (match(TokenType.NOT_EQUALS)) {
            consume(TokenType.NOT_EQUALS);
            return WhereClause.Operator.NOT_EQUALS;
        } else if (match(TokenType.LESS_THAN)) {
            consume(TokenType.LESS_THAN);
            return WhereClause.Operator.LESS_THAN;
        } else if (match(TokenType.GREATER_THAN)) {
            consume(TokenType.GREATER_THAN);
            return WhereClause.Operator.GREATER_THAN;
        } else if (match(TokenType.LESS_EQUAL)) {
            consume(TokenType.LESS_EQUAL);
            return WhereClause.Operator.LESS_EQUAL;
        } else if (match(TokenType.GREATER_EQUAL)) {
            consume(TokenType.GREATER_EQUAL);
            return WhereClause.Operator.GREATER_EQUAL;
        }
        throw new RuntimeException("Expected comparison operator");
    }

    private List<String> parseIdentifierList() {
        List<String> identifiers = new ArrayList<>();
        
        identifiers.add(consume(TokenType.IDENTIFIER).getValue());
        
        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            identifiers.add(consume(TokenType.IDENTIFIER).getValue());
        }
        
        return identifiers;
    }

    private List<Object> parseValueList() {
        List<Object> values = new ArrayList<>();
        
        values.add(parseValue());
        
        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            values.add(parseValue());
        }
        
        return values;
    }

    private Map<String, Object> parseUpdateAssignments() {
        Map<String, Object> updates = new HashMap<>();
        
        String column = consume(TokenType.IDENTIFIER).getValue();
        consume(TokenType.EQUALS);
        Object value = parseValue();
        updates.put(column, value);
        
        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            column = consume(TokenType.IDENTIFIER).getValue();
            consume(TokenType.EQUALS);
            value = parseValue();
            updates.put(column, value);
        }
        
        return updates;
    }

    private Object parseValue() {
        if (match(TokenType.NUMBER)) {
            String numStr = consume(TokenType.NUMBER).getValue();
            if (numStr.contains(".")) {
                return Double.parseDouble(numStr);
            }
            return Integer.parseInt(numStr);
        } else if (match(TokenType.STRING_LITERAL)) {
            return consume(TokenType.STRING_LITERAL).getValue();
        } else if (match(TokenType.NULL)) {
            consume(TokenType.NULL);
            return null;
        }
        throw new RuntimeException("Expected value");
    }

    private boolean match(TokenType type) {
        return currentToken != null && currentToken.getType() == type;
    }

    private Token consume(TokenType type) {
        if (!match(type)) {
            throw new RuntimeException(
                String.format("Expected %s but got %s at position %d",
                    type, 
                    currentToken != null ? currentToken.getType() : "EOF",
                    currentToken != null ? currentToken.getPosition() : -1)
            );
        }
        Token token = currentToken;
        advance();
        return token;
    }

    private void advance() {
        position++;
        currentToken = position < tokens.size() ? tokens.get(position) : null;
    }
}
