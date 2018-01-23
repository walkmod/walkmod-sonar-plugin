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

import java.util.*;

import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class RemoveEmptyMethod.
 * 
 * @author mohanasundar.n
 *
 */
@RequiresSemanticAnalysis
public class RemoveEmptyMethod extends VoidVisitorAdapter<VisitorContext> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration,
	 * java.lang.Object)
	 */
	@Override
	public void visit(ClassOrInterfaceDeclaration n, VisitorContext arg) {
		List<BodyDeclaration> result = new LinkedList<BodyDeclaration>();

		List<BodyDeclaration> members = n.getMembers();
		Iterator<BodyDeclaration> it = members.iterator();
		while(it.hasNext()) {
			BodyDeclaration bodyDeclaration = it.next();
			boolean remove = false;

			if (bodyDeclaration instanceof MethodDeclaration) {
				MethodDeclaration methodDeclaration = ((MethodDeclaration) bodyDeclaration);
				int modifiers = methodDeclaration.getModifiers();

				remove = ModifierSet.isPrivate(modifiers)
						&& !hasStatements(methodDeclaration)
						&& isUnused(methodDeclaration);
			}
			if(!remove) {
				result.add(bodyDeclaration);
			}
		}
		n.setMembers(result);
		super.visit(n, arg);
	}

	private boolean hasStatements(MethodDeclaration md) {
		BlockStmt body = md.getBody();
		return body != null && body.getStmts()!= null && !body.getStmts().isEmpty();
	}

	private boolean isUnused(MethodDeclaration md) {
		return md.getUsages() == null || md.getUsages().isEmpty();
	}
}
