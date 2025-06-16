package org.example.lox;

public class Token {
    final int line;
    final TokenType type;
    final String lexeme;
    final Object literal;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.line = line;
        this.type = type;
        this.literal = literal;
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
