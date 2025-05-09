package jlox;

import java.util.List;

class Interpreter implements Stmt.Visitor<Void>, Expr.Visitor<Object> {
	private Environment environment = new Environment();
	
	void interpret(List<Stmt> statements){
		try {
			for (Stmt statement: statements){
				execute(statement);
			}
		} catch(RuntimeError error) {
			Lox.runtimeError(error);
		}
	}

	private void execute(Stmt stmt) {
		stmt.accept(this);
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}
	
	private void executeBlock(
			List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;
		try {
			for(Stmt s: statements) {
				execute(s);
			}
		} finally {
			this.environment = previous;
		}

	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt){
		evaluate(stmt.expression);
		return null;
	}

	@Override
	public Void visitIfStmt(Stmt.If stmt){
		if(isTruthy(stmt.condition))
			execute(stmt.thenBranch);
		else if(stmt.thenBranch != null)
			execute(stmt.elseBranch);
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt){
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null)
			value = evaluate(stmt.initializer);

		environment.define(stmt.name.lexeme, value);
		return null;
	}

	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		Object value = evaluate(expr.value);
		environment.assign(expr.name, value);
		return value;
	}

	@Override
	public Object visitVariableExpr(Expr.Variable expr) {
		return environment.get(expr.name);
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override public Object visitGroupingExpr(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override public Object visitUnaryExpr(Expr.Unary expr) {
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
			case BANG:
				return !isTruthy(right);
			case MINUS:
				checkNumberOperand(expr.operator, right);
				return -(double)right;
		}
		return null; // unreachable
	}

	@Override public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
			case GREATER:
				checkNumberOperands(expr.operator, right, left);
				return (double)left > (double)right;
			case GREATER_EQUAL:
				checkNumberOperands(expr.operator, right, left);
				return (double)left >= (double)right;
			case LESS:
				checkNumberOperands(expr.operator, right, left);
				return (double)left < (double)right;
			case LESS_EQUAL:
				checkNumberOperands(expr.operator, right, left);
				return (double)left <= (double)right;
			case MINUS:
				checkNumberOperands(expr.operator, right, left);
				return (double)left / (double)right;
			case PLUS:
				if (left instanceof Double && right instanceof Double) {
					checkNumberOperands(expr.operator, right, left);
					return (double)left + (double)right;
				}
				if (left instanceof String && right instanceof String) {
					checkStringOperands(expr.operator, right, left);
					return (String)left + (String)right;
				}
				break;
			case STAR:
				checkNumberOperands(expr.operator, right, left);
				return (double)left * (double)right;
			case BANG_EQUAL:
				checkNumberOperands(expr.operator, right, left);
				return !isEqual(left, right);
			case EQUAL_EQUAL:
				checkNumberOperands(expr.operator, right, left);
				return isEqual(left, right);
		}
		return null; // unreachable
	}

	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	private boolean isTruthy(Object o) {
		if (o == null) return false;
		if (o instanceof Boolean) return (Boolean)o;
		return true;
	}

	private boolean isEqual(Object o, Object p) {
		if(o == null && p == null) return true;
		if(o == null || p == null) return false;
		return o.equals(p);
	}

	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}

	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;
		throw new RuntimeError(operator, "Operands must be numbers.");
	}

	private void checkStringOperands(Token operator, Object left, Object right) {
		if (left instanceof String && right instanceof String) return;
		throw new RuntimeError(operator, "Operands must be strings.");
	}

	private String stringify(Object object) {
		if(object == null) return "nil";
		if(object instanceof Double) {
			String text = object.toString();
			if(text.endsWith(".0")) 
				text = text.substring(0, text.length() - 2);
			return text;
		}
		return object.toString();
	}
}
