package jlox;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import static jlox.TokenType.*;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();

	private int start = 0;
	private int current = 0;
	private int line = 1;

	private static final Map<String, TokenType> keywords;
	static{
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while(!isAtEnd()) {
			// We are at the beginning of the next language
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private void scanToken(){
		char c = advance();
		switch(c) {
			// single character tokens
			case '(': addToken(LEFT_PAREN); break;
			case ')': addToken(RIGHT_PAREN); break;
			case '{': addToken(LEFT_BRACE); break;
			case '}': addToken(RIGHT_BRACE); break;
			case ',': addToken(COMMA); break;
			case '.': addToken(DOT); break;
			case '-': addToken(MINUS); break;
			case '+': addToken(PLUS); break;
			case ';': addToken(SEMICOLON); break;
			case '*': addToken(STAR); break;

			// one or two character tokens
			case '!': addToken(match('=')? BANG_EQUAL : BANG); break;
			case '=': addToken(match('=')? EQUAL_EQUAL : EQUAL); break;
			case '<': addToken(match('=')? LESS_EQUAL : LESS); break;
			case '>': addToken(match('=')? GREATER_EQUAL : GREATER); break;

			// comments
			case '/':
								if(match('/'))
								// a comment goes till the end of the line
									while(peek() != '\n' && !isAtEnd())
										advance();
								else 
									addToken(SLASH);
								break;

			// skip over other meaningless characters
			case ' ':
			case '\r':
			case '\t':
								// ignore whitespace
								break;
			case '\n':
								line++;
								break;

			// string literals
			case '"':
								string();
								break;

			default:
				// numbers
				if(isDigit(c)){
					number();
					break;
				} else if(isAlpha(c)) {
					identifier();
					break;
				}

				Lox.error(line, "Unexpected character.");
				//break;
		}
	}

	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if(peek() == '\n')
				line++;
			advance();
		}

		if(isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}

		// the closing "
		advance();

		// trim the surronding quotes
		String value = source.substring(start +1, current -1);
		addToken(STRING, value);
	}

	private void number() {
		while(isDigit(peek()))
			advance();

		// look for a fractional part	
		if (peek() == '.' && isDigit(peekNext())){
			advance(); // consume the "."

			while(isDigit(peek()))
				advance();
		}

		addToken(NUMBER,
				Double.parseDouble(source.substring(start, current)));
	}

	private void identifier() {
		while(isAlphanumeric(peek()))
			advance();

		String text = source.substring(start, current);
		TokenType type = keywords.get(text);
		if(type==null)
			type = IDENTIFIER;
		addToken(type);
	}

	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;

		current++;
		return true;
	}

	private char peek() {
		if (isAtEnd())
			return '\0';
		return source.charAt(current);
	}

	private char peekNext() {
		if (source.length() <= current+1)
			return '\0';
		return source.charAt(current+1);
	}

	private boolean isDigit(char c){
		return c >= '0' && c <= '9';
	}

	private boolean isAlpha(char c) {
		if(c=='_' || c>='a' && c<='z' || c>='A' && c<='Z')
			return true;
		return false;
	}

	private boolean isAlphanumeric(char c) {
		return isDigit(c) || isAlpha(c);
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private char advance() {
		current++;
		return source.charAt(current - 1);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

}
