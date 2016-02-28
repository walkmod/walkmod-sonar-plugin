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
import java.util.StringTokenizer;

import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.sonar.utils.Util;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class EqualsAndNotEqualsHandler.
 * 
 * @author mohanasundar.n
 *
 */
public abstract class EqualsAndNotEqualsHandler extends VoidVisitorAdapter<VisitorContext> {

	/** The update equals. */
	private boolean updateEquals;

	/** The update not equals. */
	private boolean updateNotEquals;

	/** The file name. */
	protected String fileName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang .ast.CompilationUnit,
	 * java.lang.Object)
	 */
	public void visit(CompilationUnit n, VisitorContext arg) {
		fileName = Util.getClassName(n);
		super.visit(n, arg);
	}

	/**
	 * Sets the enable for.
	 *
	 * @param enableFor
	 *            the new enable for
	 */
	public void setEnableFor(String enableFor) {
		if (enableFor != null && !enableFor.isEmpty()) {
			StringTokenizer tokenizer = new StringTokenizer(enableFor, ",");
			List<String> list = new ArrayList<String>();
			while (tokenizer.hasMoreTokens()) {
				String string = tokenizer.nextToken().trim();
				list.add(string);
			}
			setUpdateEquals(list.remove("equals"));
			setUpdateNotEquals(list.remove("notEquals"));
		}
	}

	/**
	 * Checks if is update equals.
	 *
	 * @return true, if is update equals
	 */
	protected boolean isUpdateEquals() {
		return updateEquals;
	}

	/**
	 * Sets the update equals.
	 *
	 * @param updateEquals
	 *            the new update equals
	 */
	private void setUpdateEquals(boolean updateEquals) {
		this.updateEquals = updateEquals;
	}

	/**
	 * Checks if is update not equals.
	 *
	 * @return true, if is update not equals
	 */
	protected boolean isUpdateNotEquals() {
		return updateNotEquals;
	}

	/**
	 * Sets the update not equals.
	 *
	 * @param updateNotEquals
	 *            the new update not equals
	 */
	private void setUpdateNotEquals(boolean updateNotEquals) {
		this.updateNotEquals = updateNotEquals;
	}

	/**
	 * Gets the method call expr.
	 *
	 * @param e
	 *            the e
	 * @return the method call expr
	 */
	protected Expression getMethodCallExpr(BinaryExpr e) {
		if (!isUpdateEquals() && !isUpdateNotEquals() || !isValidOperator(e)) {
			return null;
		}
		return applyChanges(e);
	}

	/**
	 * Checks if is valid operator.
	 *
	 * @param e
	 *            the e
	 * @return true, if is valid operator
	 */
	protected abstract boolean isValidOperator(BinaryExpr e);

	/**
	 * Apply changes.
	 *
	 * @param e
	 *            the e
	 * @return the expression
	 */
	protected abstract Expression applyChanges(BinaryExpr e);
}
