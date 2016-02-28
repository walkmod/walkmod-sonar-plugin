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
import java.util.Arrays;
import java.util.List;

import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class RemoveEmptyMethod.
 * 
 * @author mohanasundar.n
 *
 */
public class RemoveEmptyMethod extends VoidVisitorAdapter<VisitorContext> {

	/** The remove default. */
	private boolean removeDefault;

	/** The remove public. */
	private boolean removePublic;

	/** The remove private. */
	private boolean removePrivate = true;

	/** The remove protected. */
	private boolean removeProtected;

	/**
	 * Sets the method.
	 *
	 * @param method
	 *            the new method
	 */
	public void setMethod(String method) {
		List<String> list = new ArrayList<String>(Arrays.asList(method.split(",")));
		setRemoveDefault(list.remove("default"));
		setRemovePublic(list.remove("public"));
		setRemovePrivate(list.remove("private"));
		setRemoveProtected(list.remove("protected"));
	}

	/**
	 * Checks if is removes the default.
	 *
	 * @return true, if is removes the default
	 */
	public boolean isRemoveDefault() {
		return removeDefault;
	}

	/**
	 * Sets the removes the default.
	 *
	 * @param removeDefault
	 *            the new removes the default
	 */
	public void setRemoveDefault(boolean removeDefault) {
		this.removeDefault = removeDefault;
	}

	/**
	 * Checks if is removes the public.
	 *
	 * @return true, if is removes the public
	 */
	public boolean isRemovePublic() {
		return removePublic;
	}

	/**
	 * Sets the removes the public.
	 *
	 * @param removePublic
	 *            the new removes the public
	 */
	public void setRemovePublic(boolean removePublic) {
		this.removePublic = removePublic;
	}

	/**
	 * Checks if is removes the private.
	 *
	 * @return true, if is removes the private
	 */
	public boolean isRemovePrivate() {
		return removePrivate;
	}

	/**
	 * Sets the removes the private.
	 *
	 * @param removePrivate
	 *            the new removes the private
	 */
	public void setRemovePrivate(boolean removePrivate) {
		this.removePrivate = removePrivate;
	}

	/**
	 * Checks if is removes the protected.
	 *
	 * @return true, if is removes the protected
	 */
	public boolean isRemoveProtected() {
		return removeProtected;
	}

	/**
	 * Sets the removes the protected.
	 *
	 * @param removeProtected
	 *            the new removes the protected
	 */
	public void setRemoveProtected(boolean removeProtected) {
		this.removeProtected = removeProtected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration,
	 * java.lang.Object)
	 */
	@Override
	public void visit(ClassOrInterfaceDeclaration n, VisitorContext arg) {
		List<BodyDeclaration> members = n.getMembers();
		for (int i = 0; i < members.size();) {
			BodyDeclaration bodyDeclaration = members.get(i);
			if (bodyDeclaration instanceof MethodDeclaration) {
				MethodDeclaration methodDeclaration = ((MethodDeclaration) bodyDeclaration);
				int modifiers = methodDeclaration.getModifiers();
				boolean isPublic = ModifierSet.isPublic(modifiers);
				if (isPublic) {
					if (!isRemovePublic()) {
						i++;
						continue;
					}
				}
				boolean isPrivate = ModifierSet.isPrivate(modifiers);
				if (isPrivate) {
					if (!isRemovePrivate()) {
						i++;
						continue;
					}
				}
				boolean isProtected = ModifierSet.isProtected(modifiers);
				if (isProtected) {
					if (!isRemoveProtected()) {
						i++;
						continue;
					}
				}
				if (!isPublic && !isPrivate && !isProtected) {
					if (!isRemoveDefault()) {
						i++;
						continue;
					}
				}
				BlockStmt body = methodDeclaration.getBody();
				if (body != null && body.getStmts() == null) {
					members.remove(i);
					continue;
				}
			}
			i++;
		}
		super.visit(n, arg);
	}
}
