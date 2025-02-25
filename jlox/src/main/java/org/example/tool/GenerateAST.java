package org.example.tool;

import java.util.List;
import java.util.Arrays;
import java.io.PrintWriter;
import java.io.IOException;

class GenerateAST {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: GenerateAST [dir]");
            System.exit(64);
        }
        String outDir = args[0];
        try {
            defineAST(outDir, "Expr", Arrays.asList(
                    "Assign   : Token name, Expr value",
                    "Binary   : Expr left, Token operator, Expr right",
                    "Call     : Expr callee, Token paren, List<Expr> arguments",
                    "Grouping : Expr expression",
                    "Literal  : Object value",
                    "Logical  : Expr left, Token operator, Expr right",
                    "Unary    : Token operator, Expr right",
                    "Variable : Token name"
            ));
            defineAST(outDir, "Stmt", Arrays.asList(
                    "Block     : List<Stmt> statements",
                    "Expression: Expr expression",
                    "If        : Expr condition, Stmt thenBranch, Stmt elseBranch",
                    "Print     : Expr expression",
                    "Return    : Token keyword, Expr value",
                    "Var       : Token name, Expr Initializer",
                    "While     : Expr condition, Stmt body",
                    "Function  : Token name, List<Token> params, List<Stmt> body"
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void defineAST(String outDir, String baseName, List<String> types) throws IOException {
        String path = outDir + "/" + baseName + ".java";
        PrintWriter pw = new PrintWriter(path, "UTF-8");

        pw.println("package org.example.lox;");
        pw.println();
        pw.println("import java.util.List;");
        pw.println();
        pw.println("abstract class " + baseName  + " {");

        defineVisitor(pw, baseName, types);

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(pw, baseName, typeName, fields);
        }

        pw.println("  abstract <R> R accept(Visitor<R> visitor);");

        pw.println("}");
        pw.close();
    }

    private static void defineType(PrintWriter pw, String baseName, String typeName, String fields) {
        pw.println("\tstatic class " + typeName + " extends " + baseName + " {");
        pw.println("\t\t" + typeName + " (" + fields + ") { ");

        String[] fieldList = fields.split(", ");
        for (String field: fieldList) {
            String name = field.split(" ")[1].trim();
            pw.println("\t\t\tthis." + name + " = " + name + ";");
        }
        pw.println("\t\t}");

        pw.println();
        for (String field: fieldList) {
            String name = field.trim();
            pw.println("\t\tfinal " + name + ";");
        }

        pw.println();
        pw.println("\t\t@Override");
        pw.println("\t\t<R> R accept (Visitor<R> visitor) {");
        pw.println("\t\t\treturn visitor.visit" + typeName + baseName + "(this);");
        pw.println("\t\t}");

        pw.println("\t}");
        pw.println();
    }

    private static void defineVisitor(PrintWriter pw, String baseName, List<String> types) {
        pw.println("\tinterface Visitor<R> {");

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            pw.println(
                    "\t\tR visit" + typeName + baseName + " (" + typeName + " " + baseName.toLowerCase() + ");"
            );
        }
        pw.println(("\t}"));
    }
}