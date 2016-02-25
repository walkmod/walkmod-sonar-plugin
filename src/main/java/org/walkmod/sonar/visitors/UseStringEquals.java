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

import static org.walkmod.sonar.utils.Util.BINARY_EQUALS_OPERATOR;
import static org.walkmod.sonar.utils.Util.BINARY_NOT_EQUALS_OPERATOR;
import static org.walkmod.sonar.utils.Util.UNARY_NOT_OPERATOR;
import static org.walkmod.sonar.utils.Util.isStringLiteralExpr;

import java.util.ArrayList;
import java.util.List;

import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.BinaryExpr.Operator;
import org.walkmod.javalang.ast.expr.ConditionalExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.javalang.ast.stmt.DoStmt;
import org.walkmod.javalang.ast.stmt.ForStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.WhileStmt;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class UseStringEquals.
 *
 * @author mohanasundar.n
 */
public class UseStringEquals extends EqualsAndNotEqualsHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.metricstream.walkmod.plugin.visitors.EqualsAndNotEqualsHandler#isValidOperator(org.walkmod.javalang.ast.expr.
	 * BinaryExpr)
	 */
	@Override
	protected boolean isValidOperator(BinaryExpr e) {
		Operator op = e.getOperator();
		return op == BINARY_EQUALS_OPERATOR || op == BINARY_NOT_EQUALS_OPERATOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.metricstream.walkmod.plugin.visitors.EqualsAndNotEqualsHandler#applyChanges(org.walkmod.javalang.ast.expr.
	 * BinaryExpr)
	 */
	@Override
	protected Expression applyChanges(BinaryExpr e) {
		Expression l = e.getLeft();
		Expression r = e.getRight();
		Operator op = e.getOperator();
		Expression expression = null;
		if (isStringLiteralExpr(r)) {
			expression = applyEquals(r, l, op);
		} else if (isStringLiteralExpr(l)) {
			expression = applyEquals(l, r, op);
		}
		
		return expression;
	}

	
	/**
	 * Apply equals.
	 *
	 * @param exp
	 *            the exp
	 * @param param
	 *            the param
	 * @param op
	 *            the op
	 * @return the expression
	 */
	private Expression applyEquals(Expression exp, Expression param, Operator op) {
		Expression expression = null;
		if (op == BINARY_EQUALS_OPERATOR) {
			if (isUpdateEquals()) {
				List<Expression> p = new ArrayList<Expression>();
				p.add(param);
				expression = new MethodCallExpr(exp, "equals", p);
			}
		} else if (op == BINARY_NOT_EQUALS_OPERATOR) {
			if (isUpdateNotEquals()) {
				List<Expression> p = new ArrayList<Expression>();
				p.add(param);
				MethodCallExpr callExpr = new MethodCallExpr(exp, "equals", p);
				UnaryExpr unaryExpr = new UnaryExpr();
				unaryExpr.setExpr(callExpr);
				unaryExpr.setOperator(UNARY_NOT_OPERATOR);
				expression = unaryExpr;
			}
		}
		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.EnclosedExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(EnclosedExpr n, VisitorContext arg) {
		Expression e = n.getInner();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setInner(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.BinaryExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(BinaryExpr n, VisitorContext arg) {
		Expression e = n.getLeft();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setLeft(expression);
			}
		}
		e = n.getRight();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setRight(expression);
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
		Expression e = n.getCondition();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
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
		Expression e = n.getCondition();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
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
		Expression e = n.getCompare();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setCompare(expression);
			}
		}
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
		Expression e = n.getCondition();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
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
		Expression e = n.getCondition();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		super.visit(n, arg);
	}
}
