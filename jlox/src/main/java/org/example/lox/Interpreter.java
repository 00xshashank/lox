package org.example.lox;

public class Interpreter implements Expr.Visitor<Object>{
    public void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError err) {
            Lox.runtimeError(err);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type) {
            case TokenType.MINUS:
                checkNumberOperand(expr.operator, right);
                return (double)left - (double)right;
            case TokenType.STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case TokenType.SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case TokenType.PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                } else if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                } else { return null; }
            case TokenType.GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case TokenType.GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case TokenType.LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case TokenType.LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case TokenType.BANG_EQUAL: return !isEqual(left, right);
            case TokenType.EQUAL_EQUAL: return isEqual(left, right);
        } return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case TokenType.MINUS -> -(double) right;
            case TokenType.BANG -> !isTruthy(right);
            default -> null;
        };
    }

    private Object evaluate(Expr expr) {
        System.out.println("Evaluating expr: " + expr);
        return expr.accept(this);
    }

    private boolean isTruthy(Object obj) {
        if (obj == null) { return false; }
        if (obj instanceof Boolean) { return (boolean)obj; }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) { return true; }
        if (a == null) { return true; }
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object right) {
        if (right instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be two numbers or two string.");
    }

    public String stringify(Object value) {
        if (value == null) return "nil";

        if (value instanceof Double) {
            String txt = value.toString();
            if (txt.endsWith(".0")) {
                txt = txt.substring(0, txt.length()-2);
            }
            return txt;
        }
        return value.toString();
    }
}
