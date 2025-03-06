package org.example.lox;

import java.util.Map;
import java.util.HashMap;

public class LoxInstance {
    private final LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    public String toString() {
        return klass.name + " instance";
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }
        throw new RuntimeError(name, "Undefined property, '" + name.lexeme + "'.");
    }
}
