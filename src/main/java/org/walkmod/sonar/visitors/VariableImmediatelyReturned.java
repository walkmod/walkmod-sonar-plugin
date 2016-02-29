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

import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.AssignExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.ast.stmt.ReturnStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class VariableImmediatelyReturned.
 * 
 * @author mohanasundar.n
 *
 */
public class VariableImmediatelyReturned extends VoidVisitorAdapter<VisitorContext> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang .ast.stmt.BlockStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(BlockStmt n, VisitorContext arg) {
		List<Statement> stmts = n.getStmts();
		if (stmts == null) {
			super.visit(n, arg);
			return;
		}
		int returnStmtIndex = -1;
		String variableName = null;
		for (int i = 0; i < stmts.size(); i++) {
			Statement statement = stmts.get(i);
			if (statement instanceof ReturnStmt) {
				Expression expr = ((ReturnStmt) statement).getExpr();
				if (expr instanceof NameExpr) {
					returnStmtIndex = i;
					variableName = ((NameExpr) expr).getName();
					break;
				}
			}
		}
		if (returnStmtIndex > 0) {
			Statement statement = stmts.get(returnStmtIndex - 1);
			boolean isEmpty = false;
			boolean isRemove = false;
			Expression init = null;
			if (statement instanceof ExpressionStmt) {
				Expression expression = ((ExpressionStmt) statement).getExpression();
				if (expression instanceof VariableDeclarationExpr) {
					List<VariableDeclarator> vars = ((VariableDeclarationExpr) expression).getVars();
					for (VariableDeclarator variableDeclarator : vars) {
						if (variableDeclarator.getId().getName().equals(variableName)) {
							init = variableDeclarator.getInit();
							vars.remove(variableDeclarator);
							isRemove = true;
							break;
						}
					}
					isEmpty = vars.isEmpty();
				} else if (expression instanceof AssignExpr) {
					AssignExpr assignExpr = (AssignExpr) expression;
					if (assignExpr.getOperator() == AssignExpr.Operator.assign) {
						Expression target = assignExpr.getTarget();
						if (target instanceof NameExpr) {
							NameExpr expr = (NameExpr) target;
							if (expr.getName().equals(variableName)) {
								init = assignExpr.getValue();
								isRemove = true;
								isEmpty = true;
							}
						}
					}
				}
				if (isRemove) {
					ReturnStmt returnStmt = (ReturnStmt) stmts.get(returnStmtIndex);
					returnStmt.setExpr(init);
					if (isEmpty) {
						stmts.remove(returnStmtIndex - 1);
					}
				}
			}
		}
		super.visit(n, arg);
	}
}
