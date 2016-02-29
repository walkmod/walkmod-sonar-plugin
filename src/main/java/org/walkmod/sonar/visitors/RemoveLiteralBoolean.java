/*
 * SYSTEMi Copyright © 2015, MetricStream, Inc. All rights reserved.
 * 
 * Walkmod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Walkmod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mohanasundar N(mohanasundar.n@metricstream.com)
 * created 05/01/2015
 */

package org.walkmod.sonar.visitors;

import java.util.List;

import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.BinaryExpr.Operator;
import org.walkmod.javalang.ast.expr.BooleanLiteralExpr;
import org.walkmod.javalang.ast.expr.ConditionalExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.InstanceOfExpr;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.DoStmt;
import org.walkmod.javalang.ast.stmt.ForStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.WhileStmt;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.ast.type.PrimitiveType;
import org.walkmod.javalang.ast.type.PrimitiveType.Primitive;
import org.walkmod.javalang.ast.type.ReferenceType;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class RemoveLiteralBoolean.
 * 
 * @author mohanasundar.n
 *
 */
public class RemoveLiteralBoolean extends VoidVisitorAdapter<VisitorContext> {

	/**
	 * Gets the expression.
	 *
	 * @param expression
	 *            the expression
	 * @return the expression
	 */
	private Expression getExpression(Expression expression) {
		if (expression == null) {
			return null;
		}
		Expression e = expression;
		do {
			if (!(e instanceof BinaryExpr)) {
				break;
			}
			Expression temp = getExpression((BinaryExpr) e);
			if (temp == null) {
				break;
			}
			e = temp;
		} while (true);
		return e;
	}

	/**
	 * Gets the expression.
	 *
	 * @param binaryExpr
	 *            the binary expr
	 * @return the expression
	 */
	private Expression getExpression(BinaryExpr binaryExpr) {
		Expression left = binaryExpr.getLeft();
		Expression right = binaryExpr.getRight();
		Operator operator = binaryExpr.getOperator();
		return getExpression(left, right, operator);
	}

	/**
	 * Gets the expression.
	 *
	 * @param left
	 *            the left
	 * @param right
	 *            the right
	 * @param op
	 *            the op
	 * @return the expression
	 */
	private Expression getExpression(Expression left, Expression right, Operator op) {
		boolean lBool = left instanceof BooleanLiteralExpr;
		boolean rBool = right instanceof BooleanLiteralExpr;
		if (!lBool && !rBool) {
			return null;
		}
		return getExpression(left, right, op, lBool);
	}

	/**
	 * Gets the expression.
	 *
	 * @param left
	 *            the left
	 * @param right
	 *            the right
	 * @param op
	 *            the op
	 * @param isLeft
	 *            the is left
	 * @return the expression
	 */
	private Expression getExpression(Expression left, Expression right, Operator op, boolean isLeft) {
		if (!isLeft) {
			Expression temp = left;
			left = right;
			right = temp;
		}
		if (((BooleanLiteralExpr) left).getValue()) {
			if (op == BinaryExpr.Operator.or) {
				BooleanLiteralExpr expr = new BooleanLiteralExpr(true);
				return expr;
			} else if (op == BinaryExpr.Operator.notEquals) {
				UnaryExpr expr = new UnaryExpr(right, UnaryExpr.Operator.not);
				return expr;
			}
			return getExpression(right);
		} else {
			if (op == BinaryExpr.Operator.equals) {
				if (right instanceof InstanceOfExpr) {
					EnclosedExpr enclosedExpr = new EnclosedExpr();
					enclosedExpr.setInner(right);
					right = enclosedExpr;
				}
				UnaryExpr expr = new UnaryExpr(right, UnaryExpr.Operator.not);
				return expr;
			} else if (op == BinaryExpr.Operator.and) {
				BooleanLiteralExpr expr = new BooleanLiteralExpr(false);
				return expr;
			}
			return getExpression(right);
		}
	}

	/**
	 * Update expression.
	 *
	 * @param type
	 *            the type
	 * @param variables
	 *            the variables
	 */
	private void updateExpression(Type type, List<VariableDeclarator> variables) {
		boolean isBooleanType = false;
		if (type instanceof PrimitiveType) {
			if (((PrimitiveType) type).getType() == Primitive.Boolean) {
				isBooleanType = true;
			}
		} else if (type instanceof ReferenceType) {
			ReferenceType rtype = (ReferenceType) type;
			if (rtype.getType() instanceof ClassOrInterfaceType) {
				ClassOrInterfaceType type2 = (ClassOrInterfaceType) rtype.getType();
				if (type2.getName().equals("Boolean")) {
					isBooleanType = true;
				}
			}
		}
		if (isBooleanType && variables != null) {
			for (VariableDeclarator variableDeclarator : variables) {
				Expression expression = variableDeclarator.getInit();
				if (expression instanceof BinaryExpr) {
					expression = getExpression((BinaryExpr) expression);
					if (expression != null) {
						variableDeclarator.setInit(expression);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.FieldDeclaration,
	 * java.lang.Object)
	 */
	public void visit(FieldDeclaration n, VisitorContext arg) {
		updateExpression(n.getType(), n.getVariables());
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.VariableDeclarationExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(VariableDeclarationExpr n, VisitorContext arg) {
		updateExpression(n.getType(), n.getVars());
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.IfStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(IfStmt n, VisitorContext arg) {
		Expression condition = n.getCondition();
		if (condition instanceof BinaryExpr) {
			Expression expression = getExpression((BinaryExpr) condition);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.ForStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(ForStmt n, VisitorContext arg) {
		Expression condition = n.getCompare();
		if (condition instanceof BinaryExpr) {
			Expression expression = getExpression((BinaryExpr) condition);
			if (expression != null) {
				n.setCompare(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.WhileStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(WhileStmt n, VisitorContext arg) {
		Expression condition = n.getCondition();
		if (condition instanceof BinaryExpr) {
			Expression expression = getExpression((BinaryExpr) condition);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.DoStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(DoStmt n, VisitorContext arg) {
		Expression condition = n.getCondition();
		if (condition instanceof BinaryExpr) {
			Expression expression = getExpression((BinaryExpr) condition);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.ConditionalExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(ConditionalExpr n, VisitorContext arg) {
		Expression condition = n.getCondition();
		if (condition instanceof BinaryExpr) {
			Expression expression = getExpression((BinaryExpr) condition);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		Expression elseExpr = n.getElseExpr();
		if (elseExpr instanceof BinaryExpr) {
			Expression expression = getExpression((BinaryExpr) elseExpr);
			if (expression != null) {
				n.setElseExpr(expression);
			}
		}
		Expression thenExpr = n.getThenExpr();
		if (thenExpr instanceof BinaryExpr) {
			Expression expression = getExpression((BinaryExpr) thenExpr);
			if (expression != null) {
				n.setThenExpr(expression);
			}
		}
		super.visit(n, arg);
	}
}
