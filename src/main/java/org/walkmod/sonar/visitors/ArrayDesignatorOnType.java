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
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.type.ReferenceType;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class ArrayDesignatorOnType.
 *
 * @author mohanasundar.n
 */
public class ArrayDesignatorOnType extends VoidVisitorAdapter<VisitorContext> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.FieldDeclaration,
	 * java.lang.Object)
	 */
	@Override
	public void visit(FieldDeclaration n, VisitorContext arg) {
		List<VariableDeclarator> list = n.getVariables();
		ReferenceType referenceType = getReferenceType(list, n.getType());
		if (referenceType != null) {
			n.setType(referenceType);
		}
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
		List<VariableDeclarator> list = n.getVars();
		ReferenceType referenceType = getReferenceType(list, n.getType());
		if (referenceType != null) {
			n.setType(referenceType);
		}
		super.visit(n, arg);
	}

	/**
	 * Gets the reference type.
	 *
	 * @param list
	 *            the list
	 * @param t
	 *            the t
	 * @return the reference type
	 */
	private ReferenceType getReferenceType(List<VariableDeclarator> list, Type t) {
		ReferenceType referenceType = null;
		int arrayCount = -1;
		boolean replace = false;
		for (int i = 0; i < list.size(); i++) {
			VariableDeclarator vd = list.get(i);
			int count = vd.getId().getArrayCount();
			if (arrayCount == -1 || arrayCount == count) {
				arrayCount = count;
				if (arrayCount > 0) {
					replace = true;
				}
			} else {
				replace = false;
				break;
			}
		}
		if (replace) {
			referenceType = new ReferenceType();
			referenceType.setArrayCount(arrayCount);
			referenceType.setType(t);
			for (int i = 0; i < list.size(); i++) {
				VariableDeclarator vd = list.get(i);
				VariableDeclaratorId id = vd.getId();
				id.setArrayCount(0);
			}
		}
		return referenceType;
	}
}
