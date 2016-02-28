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

import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.ObjectCreationExpr;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class PrimitiveInstantiationForToString.
 * 
 * @author mohanasundar.n
 *
 */
public class PrimitiveInstantiationForToString extends VoidVisitorAdapter<VisitorContext> {

	/**
	 * The Enum Wrappers.
	 */
	private enum Wrappers {

		/** The byte. */
		BYTE("Byte"),

		/** The long. */
		LONG("Long"),

		/** The integer. */
		INTEGER("Integer"),

		/** The short. */
		SHORT("Short"),

		/** The float. */
		FLOAT("Float"),

		/** The double. */
		DOUBLE("Double"),

		/** The character. */
		CHARACTER("Character"),

		/** The boolean. */
		BOOLEAN("Boolean");

		/** The name. */
		String name;

		/**
		 * Instantiates a new wrappers.
		 *
		 * @param name
		 *            the name
		 */
		private Wrappers(String name) {
			this.name = name;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang .ast.expr.MethodCallExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(MethodCallExpr n, VisitorContext arg) {
		Expression scope = n.getScope();
		if (n.getName().equals("toString")) {
			if (scope instanceof ObjectCreationExpr) {
				ClassOrInterfaceType type = ((ObjectCreationExpr) scope).getType();
				String name = type.getName();
				for (Wrappers wrapper : Wrappers.values()) {
					if (name.equals(wrapper.name)) {
						MethodCallExpr e = new MethodCallExpr();
						e.setScope(new NameExpr(wrapper.name));
						e.setName("valueOf");
						e.setArgs(((ObjectCreationExpr) scope).getArgs());
						n.setScope(e);
					}
				}
			}
		}
		super.visit(n, arg);
	}
}
