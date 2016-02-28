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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.ConstructorDeclaration;
import org.walkmod.javalang.ast.body.EnumDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.AssignExpr;
import org.walkmod.javalang.ast.expr.BooleanLiteralExpr;
import org.walkmod.javalang.ast.expr.DoubleLiteralExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.FieldAccessExpr;
import org.walkmod.javalang.ast.expr.IntegerLiteralExpr;
import org.walkmod.javalang.ast.expr.LongLiteralExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.NullLiteralExpr;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class StaticInitializedFieldToFinal.
 *
 * @author mohanasundar.n
 */
public class StaticInitializedFieldToFinal extends VoidVisitorAdapter<VisitorContext> {

	/** The m static fd. */
	private Map<String, FieldDeclaration> mStaticFD;

	/** The interface stack. */
	private Stack<Boolean> interfaceStack;

	/** Remove default value from fields. */
	private boolean removeDefaultValue;

	/** Added static final to interface fields. */
	private boolean addStaticFinal = true;

	/**
	 * Instantiates a new static initialized field to final.
	 */
	public StaticInitializedFieldToFinal() {
		mStaticFD = new HashMap<String, FieldDeclaration>();
		interfaceStack = new Stack<Boolean>();
	}

	/**
	 * Checks if is removes the default value.
	 *
	 * @return true, if is removes the default value
	 */
	public boolean isRemoveDefaultValue() {
		return removeDefaultValue;
	}

	/**
	 * Sets the removes the default value.
	 *
	 * @param removeDefaultValue
	 *            the new removes the default value
	 */
	public void setRemoveDefaultValue(boolean removeDefaultValue) {
		this.removeDefaultValue = removeDefaultValue;
	}

	/**
	 * Checks if is adds the static final.
	 *
	 * @return true, if is adds the static final
	 */
	public boolean isAddStaticFinal() {
		return addStaticFinal;
	}

	/**
	 * Sets the adds the static final.
	 *
	 * @param addStaticFinal
	 *            the new adds the static final
	 */
	public void setAddStaticFinal(boolean addStaticFinal) {
		this.addStaticFinal = addStaticFinal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.CompilationUnit,
	 * java.lang.Object)
	 */
	@Override
	public void visit(CompilationUnit n, VisitorContext arg) {
		super.visit(n, arg);

		Collection<FieldDeclaration> values = mStaticFD.values();
		for (Iterator<FieldDeclaration> it = values.iterator(); it.hasNext();) {
			FieldDeclaration fDeclaration = it.next();
			if (fDeclaration == null) {
				continue;
			}
			int modifiers = fDeclaration.getModifiers();
			int sfmodifiers = ModifierSet.addModifier(modifiers, ModifierSet.FINAL);
			fDeclaration.setModifiers(sfmodifiers);
		}
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
		interfaceStack.push(n.isInterface());
		super.visit(n, arg);
		interfaceStack.pop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.EnumDeclaration,
	 * java.lang.Object)
	 */
	public void visit(EnumDeclaration n, VisitorContext arg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.FieldDeclaration,
	 * java.lang.Object)
	 */
	@Override
	public void visit(FieldDeclaration n, VisitorContext arg) {
		int modifiers = n.getModifiers();
		boolean isInterface = interfaceStack.peek();
		if (isInterface) {
			if (isAddStaticFinal()) {
				if (!ModifierSet.isPublic(modifiers)) {
					modifiers = ModifierSet.addModifier(modifiers, ModifierSet.PUBLIC);
				}
				if (!ModifierSet.isStatic(modifiers)) {
					modifiers = ModifierSet.addModifier(modifiers, ModifierSet.STATIC);
				}
				if (!ModifierSet.isFinal(modifiers)) {
					modifiers = ModifierSet.addModifier(modifiers, ModifierSet.FINAL);
				}
				n.setModifiers(modifiers);
			}
			return;
		}
		if (ModifierSet.isFinal(modifiers) || !ModifierSet.isStatic(modifiers)) {
			return;
		}
		List<VariableDeclarator> variables = n.getVariables();
		if (variables.size() != 1) {
			return;
		}
		VariableDeclarator vDeclarator = variables.get(0);
		Expression init = vDeclarator.getInit();
		if (init == null || hasDefaultValue(vDeclarator, init)) {
			return;
		}
		if (!mStaticFD.containsKey(vDeclarator.getId().getName())) {
			mStaticFD.put(vDeclarator.getId().getName(), n);
		}
	}

	/**
	 * Checks for default value.
	 *
	 * @param vDeclarator
	 *            the v declarator
	 * @param init
	 *            the init
	 * @return true, if successful
	 */
	private boolean hasDefaultValue(VariableDeclarator vDeclarator, Expression init) {
		boolean setEmpty = false;
		if (init instanceof NullLiteralExpr) {
			setEmpty = true;
		} else if (init instanceof IntegerLiteralExpr) {
			if (((IntegerLiteralExpr) init).getValue().equals("0")) {
				setEmpty = true;
			}
		} else if (init instanceof LongLiteralExpr) {
			String value = ((LongLiteralExpr) init).getValue();
			if (value.equalsIgnoreCase("0l")) {
				setEmpty = true;
			}
		} else if (init instanceof DoubleLiteralExpr) {
			String value = ((DoubleLiteralExpr) init).getValue();
			if (value.equalsIgnoreCase("0f") || value.equalsIgnoreCase("0d")) {
				setEmpty = true;
			}
		} else if (init instanceof BooleanLiteralExpr) {
			if (!((BooleanLiteralExpr) init).getValue()) {
				setEmpty = true;
			}
		}
		if (setEmpty && isRemoveDefaultValue()) {
			vDeclarator.setInit(null);
		}
		return setEmpty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.ConstructorDeclaration,
	 * java.lang.Object)
	 */
	@Override
	public void visit(ConstructorDeclaration n, VisitorContext arg) {
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.AssignExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(AssignExpr n, VisitorContext arg) {
		Expression target = n.getTarget();
		if (target instanceof NameExpr) {
			mStaticFD.put(((NameExpr) target).getName(), null);
		} else if (target instanceof FieldAccessExpr) {
			mStaticFD.put(((FieldAccessExpr) target).getField(), null);
		}
		super.visit(n, arg);
	}

}
