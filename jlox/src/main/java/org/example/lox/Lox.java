package org.example.lox;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String []args) {
        if (args.length > 1) {
            System.out.println("Usage: jlox {script]");
            System.exit(64);
        } else if (args.length == 1) {
            {
                try {
                    runFile(args[0]);
                } catch (IOException e) {
                    System.out.println("Unable to read file " + args[0]);
                    e.printStackTrace();
                }
            }
        } else {
            try {
                runPrompt();
            } catch (IOException e) {
                System.out.println("Unable to read line.");
                e.printStackTrace();
            }
        }
    }

    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) { System.exit(65); }
        if (hadRuntimeError) { System.exit(70); }
    }

    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print(">>> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String line) {
        Scanner scanner = new Scanner(line);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        if (hadError) return;

        interpreter.interpret(statements);
    }

    public static void error(int line, String message) {
        report(line, "", message);
        hadError = true;
    }

    private static void report(int line, String where, String message) {
        System.out.println("Error in line " + line + " (" + where + ") :");
        System.out.println(message);
    }

    public static void report(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end ", message);
        } else {
            report(token.line, " at '" + token.lexeme + "' ", message);
        }
    }

    public static void runtimeError(RuntimeError err) {
        System.out.println("line [" + err.token.line + "]: ");
        System.err.println(err.getMessage());
        hadRuntimeError = true;
    }
}