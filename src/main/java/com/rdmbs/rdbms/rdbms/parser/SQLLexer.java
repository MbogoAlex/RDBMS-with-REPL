package com.rdmbs.rdbms.rdbms.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLLexer {
    private final String input;
    private int position;
    private char currentChar;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("SELECT", TokenType.SELECT);
        KEYWORDS.put("INSERT", TokenType.INSERT);
        KEYWORDS.put("UPDATE", TokenType.UPDATE);
        KEYWORDS.put("DELETE", TokenType.DELETE);
        KEYWORDS.put("CREATE", TokenType.CREATE);
        KEYWORDS.put("DROP", TokenType.DROP);
        KEYWORDS.put("TABLE", TokenType.TABLE);
        KEYWORDS.put("FROM", TokenType.FROM);
        KEYWORDS.put("WHERE", TokenType.WHERE);
        KEYWORDS.put("INTO", TokenType.INTO);
        KEYWORDS.put("VALUES", TokenType.VALUES);
        KEYWORDS.put("SET", TokenType.SET);
        KEYWORDS.put("AND", TokenType.AND);
        KEYWORDS.put("OR", TokenType.OR);
        KEYWORDS.put("PRIMARY", TokenType.PRIMARY);
        KEYWORDS.put("KEY", TokenType.KEY);
        KEYWORDS.put("UNIQUE", TokenType.UNIQUE);
        KEYWORDS.put("NOT", TokenType.NOT);
        KEYWORDS.put("NULL", TokenType.NULL);
        KEYWORDS.put("INDEX", TokenType.INDEX);
        KEYWORDS.put("JOIN", TokenType.JOIN);
        KEYWORDS.put("ON", TokenType.ON);
        KEYWORDS.put("INNER", TokenType.INNER);
        KEYWORDS.put("LEFT", TokenType.LEFT);
        KEYWORDS.put("RIGHT", TokenType.RIGHT);
    }

    public SQLLexer(String input) {
        this.input = input;
        this.position = 0;
        this.currentChar = input.length() > 0 ? input.charAt(0) : '\0';
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (currentChar != '\0') {
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }

            if (Character.isLetter(currentChar) || currentChar == '_') {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            if (Character.isDigit(currentChar)) {
                tokens.add(readNumber());
                continue;
            }

            if (currentChar == '\'') {
                tokens.add(readString());
                continue;
            }

            if (currentChar == ',') {
                tokens.add(new Token(TokenType.COMMA, ",", position));
                advance();
                continue;
            }

            if (currentChar == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, ";", position));
                advance();
                continue;
            }

            if (currentChar == '(') {
                tokens.add(new Token(TokenType.LEFT_PAREN, "(", position));
                advance();
                continue;
            }

            if (currentChar == ')') {
                tokens.add(new Token(TokenType.RIGHT_PAREN, ")", position));
                advance();
                continue;
            }

            if (currentChar == '*') {
                tokens.add(new Token(TokenType.ASTERISK, "*", position));
                advance();
                continue;
            }

            if (currentChar == '=') {
                tokens.add(new Token(TokenType.EQUALS, "=", position));
                advance();
                continue;
            }

            if (currentChar == '<') {
                advance();
                if (currentChar == '=') {
                    tokens.add(new Token(TokenType.LESS_EQUAL, "<=", position - 1));
                    advance();
                } else if (currentChar == '>') {
                    tokens.add(new Token(TokenType.NOT_EQUALS, "<>", position - 1));
                    advance();
                } else {
                    tokens.add(new Token(TokenType.LESS_THAN, "<", position - 1));
                }
                continue;
            }

            if (currentChar == '>') {
                advance();
                if (currentChar == '=') {
                    tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", position - 1));
                    advance();
                } else {
                    tokens.add(new Token(TokenType.GREATER_THAN, ">", position - 1));
                }
                continue;
            }

            if (currentChar == '!') {
                advance();
                if (currentChar == '=') {
                    tokens.add(new Token(TokenType.NOT_EQUALS, "!=", position - 1));
                    advance();
                    continue;
                }
            }

            tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(currentChar), position));
            advance();
        }

        tokens.add(new Token(TokenType.EOF, "", position));
        return tokens;
    }

    private void advance() {
        position++;
        currentChar = position < input.length() ? input.charAt(position) : '\0';
    }

    private void skipWhitespace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    private Token readIdentifierOrKeyword() {
        int startPos = position;
        StringBuilder sb = new StringBuilder();

        while (currentChar != '\0' && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            sb.append(currentChar);
            advance();
        }

        String value = sb.toString();
        String upperValue = value.toUpperCase();

        TokenType type = KEYWORDS.getOrDefault(upperValue, TokenType.IDENTIFIER);
        return new Token(type, value, startPos);
    }

    private Token readNumber() {
        int startPos = position;
        StringBuilder sb = new StringBuilder();

        while (currentChar != '\0' && (Character.isDigit(currentChar) || currentChar == '.')) {
            sb.append(currentChar);
            advance();
        }

        return new Token(TokenType.NUMBER, sb.toString(), startPos);
    }

    private Token readString() {
        int startPos = position;
        StringBuilder sb = new StringBuilder();
        advance(); // Skip opening quote

        while (currentChar != '\0' && currentChar != '\'') {
            if (currentChar == '\\') {
                advance();
                if (currentChar != '\0') {
                    sb.append(currentChar);
                    advance();
                }
            } else {
                sb.append(currentChar);
                advance();
            }
        }

        if (currentChar == '\'') {
            advance(); // Skip closing quote
        }

        return new Token(TokenType.STRING_LITERAL, sb.toString(), startPos);
    }
}
