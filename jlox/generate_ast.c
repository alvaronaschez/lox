/*
expression -> literal
						| unary
						| binary
						| grouping ;

literal	-> NUMBER | STRING | "true" | "false" | "nil" ;
grouping	-> "(" expression ")" ;
unary		-> ( "-" | "!" ) expression ;
binary		-> expression operator expression ;
operator	-> "==" | "!=" | "<" | "<=" | ">" | ">=" 
					| "+" | "-" | "*" | "/" ;
*/

#include <stdlib.h> // free
#include <stdio.h> // printf
#include <string.h> // strtok strcpy strlen
#include <stdbool.h> // true false boolean

typedef struct {
	char* class_name;
	char* fields;
} class_t;

void define_ast(char* output_dir,char* base_name, class_t types[]);

int main(){
	printf("Generating 'Expr.java' ...\n\n");
	char* output_dir = ".";
	
	class_t types[] = {
		{"Binary", "Expr left, Token operator, Expr right"},
		{"Grouping", "Expr expression"},
		{"Literal", "Expr expression"},
		{"Token", "Token operator, Expr right"},
	};

	define_ast(output_dir, "Expr", types);

	return 0;
}

void define_ast(char* output_dir,char* base_name, class_t types[]) {
	char* f = types[0].fields;
	char* s = malloc(strlen(f)+1);
	strcpy(s, f);
	char* token = strtok(s, ", ");
	while(token != NULL) {
		printf("%s %s\n", token, strtok(NULL, ", "));	
		token = strtok(NULL, ", ");
	}
	free(s);
}
