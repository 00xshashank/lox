package org.example.lox;

public class ASTPrinter implements Expr.Visitor<String>{
    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.right, expr.left);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == "null") return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String operator, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(operator);
        for (Expr expr: exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
          new Expr.Unary(
                  new Token(TokenType.MINUS, "-", null, 1),
                  new Expr.Literal(123)),
          new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Literal(45.67)));

        System.out.println(new ASTPrinter().print(expression));
    }
}
