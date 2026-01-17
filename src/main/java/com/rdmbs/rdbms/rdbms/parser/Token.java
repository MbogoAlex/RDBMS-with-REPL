package com.rdmbs.rdbms.rdbms.parser;

import lombok.Data;

@Data
public class Token {
    private final TokenType type;
    private final String value;
    private final int position;

    public Token(TokenType type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, '%s', pos=%d)", type, value, position);
    }
}
