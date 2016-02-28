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

import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.EmptyStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class RemoveEmptyStatement.
 *
 * @author mohanasundar.n
 */
public class RemoveEmptyStatement extends VoidVisitorAdapter<VisitorContext> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.BlockStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(BlockStmt n, VisitorContext arg) {
		List<Statement> list = n.getStmts();
		if (list != null) {
			for (int i = 0; i < list.size();) {
				if (list.get(i) instanceof EmptyStmt) {
					list.remove(i);
					continue;
				}
				i++;
			}
			if (list.isEmpty()) {
				list = null;
			}
			n.setStmts(list);
		}
		super.visit(n, arg);
	}
}
