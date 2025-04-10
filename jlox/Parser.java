package jlox;

import java.util.List;
import java.util.ArrayList;

import static jlox.TokenType.*;

/* LOX GRAMMAR

program -> declaration* EOF ;

declaration -> varDecl | statement ;

statement -> exprStmt | printStmt ;

varDecl -> "var" IDENTIFIER ( "=" expression )? ";" ;

exprStmt -> expression ";" ;
printStmt -> "print" expression ";" ;

expression 	-> equality
equality 		-> comparison (( "!=" | "==" ) comparison )* ;
comparison 	-> term (( ">" | ">=" | "<" | "<=" ) term )* ;
term 				-> factor (( "-" | "+" ) factor )* ;
factor 			-> unary (( "/" | "*" ) unary )* ;
unary 			-> ("!" | "-")* unary | primary ;
primary 		-> NUMBER | STRING | "true" | "false" | "nil"
						| "(" expression ")" | IDENTIFIER;
*/
class Parser {
	private static class ParseError extends RuntimeException {};
	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	// program -> declaration* EOF ;
	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while(!isAtEnd()) {
			statements.add(declaration());
		}
		
		return statements;
	}

// declaration -> varDecl | statement ;
	private Stmt declaration() {
		try{
			if(match(VAR))
				return varDeclaration();

			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	// statement -> exprStmt | printStmt ;
	private Stmt statement() {
		if(match(PRINT))
			return printStatement();
		return expressionStatement();
	}

	// varDecl -> "var" IDENTIFIER ( "=" expression )? ";" ;
	private Stmt varDeclaration() {
		Token name = consume(IDENTIFIER, "Expect variable name.");

		Expr initializer = null;
		if(match(EQUAL))
			initializer = expression();

		consume(SEMICOLON, "Expect ';' after variable declaration.");
		return new Stmt.Var(name, initializer);
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
	 * expression 	-> assignment
	 */
	Expr expression() {
		return assignment();
	}

	// assignment -> IDENTIFIER "=" assignment | equality ;
	private Expr assignment() {
		Expr expr = equality();

		if(match(EQUAL)) {
			Token equals = previous();
			Expr value = assignment();

			if(expr instanceof Expr.Variable){
				Token name = ((Expr.Variable)expr).name;
				return new Expr.Assign(name, value);
			}

			error(equals, "Invalid assignment target.");
		}
		return expr;
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
		if(match(IDENTIFIER)) return new Expr.Variable(previous());
		if(match(LEFT_PAREN)){
			Expr e = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression");
			return new Expr.Grouping(e);
		}
		throw error(peek(), "Expect expression.");
	}
}

