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

import java.util.ArrayList;
import java.util.List;

import org.walkmod.javalang.ast.expr.CharLiteralExpr;
import org.walkmod.javalang.ast.expr.DoubleLiteralExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.IntegerLiteralExpr;
import org.walkmod.javalang.ast.expr.LongLiteralExpr;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class StringCheckOnLeft.
 *
 * @author mohanasundar.n
 */
public class StringCheckOnLeft extends VoidVisitorAdapter<VisitorContext> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.MethodCallExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(MethodCallExpr n, VisitorContext arg) {
		String name = n.getName();
		if (name.equals("equals") || name.equals("equalsIgnoreCase")) {
			do {
				Expression scope = n.getScope();
				if (isStringLiteralExpr(scope)) {
					break;
				}
				List<Expression> l = n.getArgs();
				if (l == null || l.size() != 1) {
					break;
				}
				Expression e = l.get(0);
				if (!isStringLiteralExpr(e)) {
					break;
				}
				n.setScope(e);
				List<Expression> args = new ArrayList<Expression>();
				args.add(scope);
				n.setArgs(args);
			} while (false);
		}
		super.visit(n, arg);
	}
	
	private boolean isStringLiteralExpr(Expression e) {
      boolean isString = e instanceof StringLiteralExpr;
      isString = isString && !(e instanceof CharLiteralExpr);
      isString = isString && !(e instanceof DoubleLiteralExpr);
      isString = isString && !(e instanceof IntegerLiteralExpr);
      isString = isString && !(e instanceof LongLiteralExpr);
      return isString;
   }
}
