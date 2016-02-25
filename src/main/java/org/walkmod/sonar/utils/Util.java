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

package org.walkmod.sonar.utils;

import java.util.List;

import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.PackageDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.TypeDeclaration;
import org.walkmod.javalang.ast.expr.CharLiteralExpr;
import org.walkmod.javalang.ast.expr.DoubleLiteralExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.IntegerLiteralExpr;
import org.walkmod.javalang.ast.expr.LongLiteralExpr;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;

/**
 * The Utility class contains utility methods and constants, which is used across the plug in API.
 *
 * @author mohanasundar.n
 */
public final class Util {

	/** The Constant UNARY_NOT_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.UnaryExpr.Operator UNARY_NOT_OPERATOR = org.walkmod.javalang.ast.expr.UnaryExpr.Operator.not;

	/** The Constant BINARY_EQUALS_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.BinaryExpr.Operator BINARY_EQUALS_OPERATOR = org.walkmod.javalang.ast.expr.BinaryExpr.Operator.equals;

	/** The Constant BINARY_NOT_EQUALS_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.BinaryExpr.Operator BINARY_NOT_EQUALS_OPERATOR = org.walkmod.javalang.ast.expr.BinaryExpr.Operator.notEquals;

	/** The Constant BINARY_GREATER_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.BinaryExpr.Operator BINARY_GREATER_OPERATOR = org.walkmod.javalang.ast.expr.BinaryExpr.Operator.greater;

	/** The Constant BINARY_LESS_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.BinaryExpr.Operator BINARY_LESS_OPERATOR = org.walkmod.javalang.ast.expr.BinaryExpr.Operator.less;

	/** The Constant BINARY_AND_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.BinaryExpr.Operator BINARY_AND_OPERATOR = org.walkmod.javalang.ast.expr.BinaryExpr.Operator.and;

	/** The Constant BINARY_OR_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.BinaryExpr.Operator BINARY_OR_OPERATOR = org.walkmod.javalang.ast.expr.BinaryExpr.Operator.or;

	/** The Constant BINARY_NOTEQUALS_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.BinaryExpr.Operator BINARY_NOTEQUALS_OPERATOR = org.walkmod.javalang.ast.expr.BinaryExpr.Operator.notEquals;

	/** The Constant ASSIGN_OPERATOR. */
	public static final org.walkmod.javalang.ast.expr.AssignExpr.Operator ASSIGN_OPERATOR = org.walkmod.javalang.ast.expr.AssignExpr.Operator.assign;

	/**
	 * Checks whether the given {@link Expression} is instance of {@link StringLiteralExpr}.
	 *
	 * @param e
	 *            The instance of {@link Expression}
	 * @return true, If is {@link StringLiteralExpr}
	 */
	public static boolean isStringLiteralExpr(Expression e) {
		boolean isString = e instanceof StringLiteralExpr;
		isString = isString && !(e instanceof CharLiteralExpr);
		isString = isString && !(e instanceof DoubleLiteralExpr);
		isString = isString && !(e instanceof IntegerLiteralExpr);
		isString = isString && !(e instanceof LongLiteralExpr);
		return isString;
	}

	/**
	 * Checks whether the given {@link Expression} is instance of {@link IntegerLiteralExpr}.
	 *
	 * @param e
	 *            The instance of {@link Expression}
	 * @return true, If is {@link IntegerLiteralExpr}
	 */
	public static boolean isIntegerLiteralExpr(Expression e) {
		return (e instanceof IntegerLiteralExpr);
	}

	/**
	 * Gives the integer value of the {@link IntegerLiteralExpr}.
	 *
	 * <p>
	 * If the value is not decimal then will be converted to decimal. Currently Hex decimal value will be converted to
	 * decimal.
	 * </p>
	 * 
	 * @param e
	 *            The instance of {@link IntegerLiteralExpr}
	 * @return The {@link IntegerLiteralExpr} value
	 */
	public static int getValue(IntegerLiteralExpr e) {
		String value = e.getValue();
		try {
			return Integer.parseInt(e.getValue());
		} catch (NumberFormatException ex) {
			if (value.toLowerCase().startsWith("0x")) {
				return Integer.parseInt(value.substring(2), 16);
			}
			throw ex;
		}
	}

	/**
	 * Gives the complete class name of the given {@link CompilationUnit}.
	 *
	 * @param n
	 *            The {@link CompilationUnit} object
	 * @return The class name with package, empty if no class found
	 */
	public static String getClassName(CompilationUnit n) {
		String fileName = "";
		List<TypeDeclaration> list = n.getTypes();
		if (list == null) {
			return fileName;
		}
		for (int i = list.size() - 1; i >= 0; i--) {
			TypeDeclaration declaration = list.get(i);
			if (ModifierSet.isPublic(declaration.getModifiers()) || i == 0) {
				PackageDeclaration packageDeclaration = n.getPackage();
				if (packageDeclaration != null) {
					fileName = packageDeclaration.getName() + ".";
				}
				fileName += declaration.getName();
			}
		}
		return fileName;
	}
}
