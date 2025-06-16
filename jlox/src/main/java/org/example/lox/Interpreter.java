package org.example.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis()/1000.0;
            }

            @Override
            public String toString() { return "clock<native fn>"; }
        });
    }

    public void interpret(List<Stmt> statemnts) {
        try {
            for (Stmt stmt: statemnts) {
                execute(stmt);
            }
        } catch (RuntimeError err) {
            Lox.runtimeError(err);
        }
    }

    private void execute(Stmt stmt) { stmt.accept(this); }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if(stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) { value = evaluate(stmt.value); }

        throw new Return(value);
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme, null);
        LoxClass klass = new LoxClass(stmt.name.lexeme);
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.Initializer != null) { value = evaluate(stmt.Initializer); }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
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
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for(Expr argument: expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can call only functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;
        if(arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
                    "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
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
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if(expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) { return left; }
        } else {
            if (!isTruthy(left)) { return left; }
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        return null;
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

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if(distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment env) {
        Environment previous = this.environment;
        try {
            this.environment = env;
            for (Stmt stmt: statements) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
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

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }
}
