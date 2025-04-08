package jlox;

import java.util.List;
import java.util.ArrayList;

import static jlox.TokenType.*;

/*
program -> statement* EOF ;

statement -> exprStmt | printStmt ;

exprStmt -> expression ";" ;
printStmt -> "print" expression ";" ;

expression 	-> equality
equality 		-> comparison (( "!=" | "==" ) comparison )* ;
comparison 	-> term (( ">" | ">=" | "<" | "<=" ) term )* ;
term 				-> factor (( "-" | "+" ) factor )* ;
factor 			-> unary (( "/" | "*" ) unary )* ;
unary 			-> ("!" | "-")* unary | primary ;
primary 		-> NUMBER | STRING | "true" | "false" | "nil"
						| "(" expression ")" ;
*/
class Parser {
	private static class ParseError extends RuntimeException {};
	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	// program -> statement* EOF ;
	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while(!isAtEnd()) {
			statements.add(statement());
		}
		
		return statements;
	}

	// statement -> exprStmt | printStmt ;
	private Stmt statement() {
		if(match(PRINT))
			return printStatement();
		return expressionStatement();
	}

	// exprStmt -> expression ";" ;
	private Stmt expressionStatement(){
		Expr expr = expression();
		consume(SEMICOLON, "Expect ';' after expression.");
		return new Stmt.Expression(expr);
	}

	// printStmt -> "print" expression ";" ;
	private Stmt printStatement(){
		Expr expr = expression();
		consume(SEMICOLON, "Expect ';' after expression.");
		return new Stmt.Print(expr);
	}


	private boolean match(TokenType ...tokenTypes){
		for(TokenType type: tokenTypes){
			if(check(type)){
				advance();
				return true;
			}
		}
		return false;
	}

	private boolean check(TokenType type) {
		//if(isAtEnd()) return false; // I don't think this is needed
		return peek().type == type;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean isAtEnd() {
		//return peek().type == EOF;
		return check(EOF);
	}

	private Token previous(){
		return this.tokens.get(current-1); 
	}

	private Token advance() {
		if(!isAtEnd())
			current++;
		return previous();
	}

	private Token consume(TokenType type, String message){
		if(check(type))
			return advance();
		throw error(peek(), message);
	}

	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}

	private void synchronize(){
		advance();

		while(!isAtEnd()) {
			if(previous().type == SEMICOLON)
				return;
			switch(peek().type){
				case CLASS:
				case FUN:
				case VAR:
				case FOR:
				case IF:
				case WHILE:
				case PRINT:
				case RETURN:
					return;
			}
			advance();
		}
	}

	/*
	 * expression 	-> equality
	 */
	Expr expression() {
		return equality();
	}

	/*
	 * equality 		-> comparison (( "!=" | "==" ) comparison )* ;
	 */
	Expr equality() {
		Expr e = comparison();

		while(match(BANG_EQUAL, EQUAL_EQUAL)) {
			e = new Expr.Binary(e, previous(), comparison());			
		}
		return e;	
	}

	/*
	 * comparison 	-> term (( ">" | ">=" | "<" | "<=" ) term )* ;
	 */
	Expr comparison() {
		Expr e = term();

		while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			e = new Expr.Binary(e, previous(), term());			
		}
		return e;	
	}

	/*
	 * term 				-> factor (( "-" | "+" ) factor )* ;
	 */
	Expr term() {
		Expr e = factor();

		while(match(MINUS, PLUS)) {
			e = new Expr.Binary(e, previous(), factor());			
		}
		return e;
	}

	/*
	 * factor 			-> unary (( "/" | "*" ) unary )* ;
	 */
	Expr factor() {
		Expr e = unary();

		while(match(SLASH, STAR)) {
			e = new Expr.Binary(e, previous(), unary());			
		}
		return e;

	}

	/*
	 * unary 			-> ("!" | "-")* unary | primary ;
	 */
	Expr unary() {
		if(match(BANG, MINUS)){
			return new Expr.Unary(previous(), unary());
		}
		return primary();
	}

	/*
	 * primary 		-> NUMBER | STRING | "true" | "false" | "nil"
	 *					| "(" expression ")" ;
	 */
	Expr primary() {
		if(match(TRUE)) return new Expr.Literal(true);
		if(match(FALSE)) return new Expr.Literal(false);
		if(match(NIL)) return new Expr.Literal(null);
		if(match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
		if(match(LEFT_PAREN)){
			Expr e = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression");
			return new Expr.Grouping(e);
		}
		throw error(peek(), "Expect expression.");
	}
}

