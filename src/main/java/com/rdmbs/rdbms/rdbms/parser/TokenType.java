package com.rdmbs.rdbms.rdbms.parser;

public enum TokenType {
    // Keywords
    SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, TABLE, FROM, WHERE, INTO, VALUES,
    SET, AND, OR, PRIMARY, KEY, UNIQUE, NOT, NULL, INDEX, JOIN, ON, INNER, LEFT, RIGHT,
    
    // Operators
    EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_EQUAL, GREATER_EQUAL,
    
    // Literals and Identifiers
    IDENTIFIER, STRING_LITERAL, NUMBER,
    
    // Symbols
    COMMA, SEMICOLON, LEFT_PAREN, RIGHT_PAREN, ASTERISK,
    
    // Special
    EOF, UNKNOWN
}
