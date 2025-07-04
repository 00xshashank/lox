package org.example.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<Token>();

    private int current = 0;
    private int line = 1;
    private int start = 0;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    Scanner (String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single character lexemes
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            // Two character relational operators
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;

            case '/':
                if (match('/')) {
                    while(peek() != '\n' && !isAtEnd()) { advance(); }
                } else if (match('*')) {
                    while (peek() != '*' && peekNext() != '/' && !isAtEnd()) { advance(); }
                    advance(); advance();
                } else {
                    addToken(TokenType.SLASH);
                } break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n': line++; break;

            case '"': string(); break;

            default:
                if (isDigit(c)) { number(); }
                else if (isAlpha(c)) { identifier(); }
                else { Lox.error(line, "Unexpected character " + c + "."); }
                break;
        }
    }

    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();
        String value = source.substring(start+1, current-1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while(isDigit(peek())) { advance(); }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while(isDigit(peek())) { advance(); }
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAplhaNumeric(peek())) { advance(); }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) { type = TokenType.IDENTIFIER; }

        addToken(type);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current+1 > source.length()) { return '\0'; }
        return source.charAt(current+1);
    }

    private boolean isAtEnd() { return (current == source.length()); }
    private char advance() { return source.charAt(current++); }
    private boolean isDigit(char c) { return (c<='9' && c>='0'); }
    private boolean isAlpha(char c) { return ((c>='A' && c<='Z') || (c>='a' && c<='z') || c=='_'); }
    private boolean isAplhaNumeric(char c) { return (isDigit(c) || isAlpha(c)); }
}
