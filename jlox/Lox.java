package jlox;

//import jlox.*;
import jlox.Scanner;
import jlox.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
	static boolean hadError = false;

	public static void main(String[] args) throws IOException {
		System.out.println("Hello Lox!");
		if(args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.exit(64);
		} else if(args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	public static void runFile(String path) throws IOException {
		//System.out.println(path);
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		if(hadError) System.exit(65);
	}

	public static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		while(true){
			System.out.print("> ");
			String line = reader.readLine();
			if(line == null) break;
			run(line);
			hadError = false;
		}

	}

	public static void run(String source){
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		Expr expression = parser.parse();
		
		// stop if there was a synthax error
		if (hadError)
			return;

		System.out.println(new AstPrinter().print(expression));
	}

	static void error(int line, String message){
		report(line, "", message);
	}

	static void report(int line, String where, String message){
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

	static void error(Token token, String message) {
		if(token.type == TokenType.EOF){
			report(token.line, "at end", message);
		} else {
			report(token.line, "at '" + token.lexeme + "'", message);
		}
	}
}
