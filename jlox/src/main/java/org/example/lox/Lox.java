package org.example.lox;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class Lox {
    static boolean hadError = false;

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

        for (Token token: tokens) { System.out.println(token); }
    }

    public static void error(int line, String message) {
        report(line, "", message);
        hadError = true;
    }

    private static void report(int line, String where, String message) {
        System.out.println("Error in line " + line + " (" + where + ") :");
        System.out.println(message);
    }
}