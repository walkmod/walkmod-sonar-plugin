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

import static org.walkmod.sonar.utils.Util.BINARY_EQUALS_OPERATOR;
import static org.walkmod.sonar.utils.Util.BINARY_GREATER_OPERATOR;
import static org.walkmod.sonar.utils.Util.BINARY_LESS_OPERATOR;
import static org.walkmod.sonar.utils.Util.BINARY_NOT_EQUALS_OPERATOR;
import static org.walkmod.sonar.utils.Util.UNARY_NOT_OPERATOR;
import static org.walkmod.sonar.utils.Util.getValue;
import static org.walkmod.sonar.utils.Util.isIntegerLiteralExpr;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.ImportDeclaration;
import org.walkmod.javalang.ast.PackageDeclaration;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.ConstructorDeclaration;
import org.walkmod.javalang.ast.body.EnumDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.BinaryExpr.Operator;
import org.walkmod.javalang.ast.expr.ConditionalExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.IntegerLiteralExpr;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.QualifiedNameExpr;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.DoStmt;
import org.walkmod.javalang.ast.stmt.ForStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.WhileStmt;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.ast.type.PrimitiveType;
import org.walkmod.javalang.ast.type.ReferenceType;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.ast.type.VoidType;
import org.walkmod.sonar.config.PropertyFileReader;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class UseCollectionIsEmpty.
 *
 * This rule address the following issues.
 * 
 * <ul>
 * <li>if <code>collection.size() == 0</code> then <code>collection.isEmpty()</code> is used</li>
 * <li>if <code>collection.size() != 0</code> then <code>!collection.isEmpty()</code> is used</li>
 * <li>if <code>collection.size() &gt; 0</code> then <code>!collection.isEmpty()</code> is used</li>
 * <li>if <code>collection.size() &lt; 1</code> then <code>collection.isEmpty()</code> is used</li>
 * </ul>
 * 
 * @author mohanasundar.n
 * 
 */
public class UseCollectionIsEmpty extends EqualsAndNotEqualsHandler {

	/** The variables map. */
	private Map<String, Map<String, Object>> variablesMap = new HashMap<String, Map<String, Object>>();

	/** The class stack. */
	private Stack<String> classStack = new Stack<String>();

	/** The method stack. */
	private Stack<String> methodStack = new Stack<String>();

	/** The cu. */
	CompilationUnit cu;

	/** The collection libraries. */
	private static List<String> collectionLibraries;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.metricstream.walkmod.plugin.visitors.EqualsAndNotEqualsHandler#isValidOperator(org.walkmod.javalang.ast.expr.
	 * BinaryExpr)
	 */
	@Override
	protected boolean isValidOperator(BinaryExpr e) {
		Operator op = e.getOperator();
		return op == BINARY_EQUALS_OPERATOR || op == BINARY_LESS_OPERATOR || op == BINARY_NOT_EQUALS_OPERATOR
				|| op == BINARY_GREATER_OPERATOR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.metricstream.walkmod.plugin.visitors.EqualsAndNotEqualsHandler#applyChanges(org.walkmod.javalang.ast.expr.
	 * BinaryExpr)
	 */
	@Override
	protected Expression applyChanges(BinaryExpr e) {
		Expression l = e.getLeft();
		Expression r = e.getRight();
		Operator op = e.getOperator();
		Expression expression = null;
		if (isIntegerLiteralExpr(r)) {
			expression = applyEquals(l, op, getValue((IntegerLiteralExpr) r));
		} else if (isIntegerLiteralExpr(l)) {
			expression = applyEquals(r, op, getValue((IntegerLiteralExpr) l));
		}
		return expression;
	}

	/**
	 * Apply equals.
	 *
	 * @param exp
	 *            the exp
	 * @param op
	 *            the op
	 * @param value
	 *            the value
	 * @return the expression
	 */
	private Expression applyEquals(Expression exp, Operator op, int value) {
		if (!(exp instanceof MethodCallExpr)) {
			return null;
		}
		MethodCallExpr mc = (MethodCallExpr) exp;
		if (!mc.getName().equals("size")) {
			return null;
		}
		if (mc.getScope() instanceof NameExpr) {
			NameExpr scope = (NameExpr) mc.getScope();
			Expression expression = null;
			if (isInstanceOfCollection(scope)) {
				if (op == BINARY_EQUALS_OPERATOR) {
					if (value == 0 && isUpdateEquals()) {
						expression = new MethodCallExpr(scope, "isEmpty");
					}
				} else if (op == BINARY_LESS_OPERATOR) {
					if (value == 1 && isUpdateEquals()) {
						expression = new MethodCallExpr(scope, "isEmpty");
					}
				} else if (op == BINARY_NOT_EQUALS_OPERATOR || op == BINARY_GREATER_OPERATOR) {
					if (value == 0 && isUpdateNotEquals()) {
						MethodCallExpr callExpr = new MethodCallExpr(scope, "isEmpty");
						UnaryExpr unaryExpr = new UnaryExpr();
						unaryExpr.setExpr(callExpr);
						unaryExpr.setOperator(UNARY_NOT_OPERATOR);
						expression = unaryExpr;
					}
				}
			}
			return expression;
		} else {
			/*
			 * We cannot handle this/MethodCallExpr. Exp: obj.get().size() == 0
			 */
			return null;
		}
	}

	/**
	 * Gets the unique method name.
	 *
	 * @param n
	 *            the n
	 * @return the unique method name
	 */
	private String getUniqueMethodName(BodyDeclaration n) {
		StringBuffer sb = new StringBuffer("m_");
		List<Parameter> parameters = null;
		if (n instanceof ConstructorDeclaration) {
			sb.append(((ConstructorDeclaration) n).getName());
			parameters = ((ConstructorDeclaration) n).getParameters();
		} else if (n instanceof MethodDeclaration) {
			sb.append(((MethodDeclaration) n).getName());
			parameters = ((MethodDeclaration) n).getParameters();
		}
		if (parameters != null) {
			for (Parameter parameter : parameters) {
				Type type = parameter.getType();
				sb.append("_").append(getActualType(type));
			}
		}
		return sb.toString();
	}

	/**
	 * Checks if is instance of collection.
	 *
	 * @param variableName
	 *            the variable name
	 * @return true, if is instance of collection
	 */
	private boolean isInstanceOfCollection(NameExpr variableName) {
		boolean isCollection = false;
		String variableType = getVariableType(variableName.getName());
		if (variableType == null) {
			return isCollection;
		}
		boolean isFoundPackage = false;
		for (ImportDeclaration declaration : cu.getImports()) {
			if (!declaration.isStatic()) {
				NameExpr expr = declaration.getName();
				if (!expr.getName().equals(variableType)) {
					continue;
				}
				isFoundPackage = true;
				StringBuffer sb = new StringBuffer(expr.getName());
				expr = ((QualifiedNameExpr) expr).getQualifier();
				do {
					if (expr == null) {
						break;
					}
					sb.insert(0, expr.getName() + ".");
					if (expr instanceof QualifiedNameExpr) {
						expr = ((QualifiedNameExpr) expr).getQualifier();
					} else {
						break;
					}
				} while (expr != null);
				isCollection = isInstanceOfCollection(sb.toString());
			}
		}
		if (!isFoundPackage) {
			PackageDeclaration declaration = cu.getPackage();
			if (declaration != null) {
			}
			isCollection = isInstanceOfCollection(variableType);
		}
		return isCollection;
	}

	/**
	 * Checks if is instance of collection.
	 *
	 * @param string
	 *            the string
	 * @return true, if is instance of collection
	 */
	private boolean isInstanceOfCollection(String string) {
		Class<?> _class = null;
		String classFullName = string;
		if (string.indexOf(".") == -1) {
			classFullName = getFullClassName(string);
		}
		try {
			_class = Class.forName(classFullName);
		} catch (Exception e) {
			System.out.println("Unable to resolve the class: " + classFullName);
			return false;
		}
		return Collection.class.isAssignableFrom(_class) || Map.class.isAssignableFrom(_class);
	}

	/**
	 * Gets the full class name.
	 *
	 * @param string
	 *            the string
	 * @return the full class name
	 */
	private String getFullClassName(String string) {
		List<ImportDeclaration> importDeclarations = cu.getImports();
		for (ImportDeclaration importDeclaration : importDeclarations) {
			if (importDeclaration.isAsterisk()) {
				String collectionPackage = importDeclaration.getName().toString();
				if (collectionPackage.equals("java.util")) {
					if (collectionLibraries.contains(string)) {
						return collectionPackage + "." + string;
					}
				}
			}
			if (importDeclaration.getName().getName().equals(string)) {
				return importDeclaration.getName().toString();
			}
		}
		String classFullName = "";
		if (cu.getPackage() != null) {
			classFullName += cu.getPackage().getName().toString() + ".";
		}
		classFullName += string;
		return classFullName;
	}

	/**
	 * Gets the variable type.
	 *
	 * @param variableName
	 *            the variable name
	 * @return the variable type
	 */
	@SuppressWarnings("rawtypes")
	private String getVariableType(String variableName) {
		if (classStack.isEmpty()) {
			return null;
		}
		Map classData = variablesMap.get(classStack.peek());
		if (!methodStack.isEmpty()) {
			Object[] strings = methodStack.toArray();
			for (int i = 0; i < strings.length; i++) {
				Map methodData = classData;
				for (int i1 = 0; i1 < strings.length - i; i1++) {
					methodData = (Map) methodData.get(strings[i1]);
				}
				if (methodData.containsKey(variableName)) {
					return (String) methodData.get(variableName);
				}
			}
		}
		return (String) classData.get(variableName);
	}

	/**
	 * Gets the actual type.
	 *
	 * @param type
	 *            the type
	 * @return the actual type
	 */
	private String getActualType(Type type) {
		if (type instanceof ClassOrInterfaceType) {
			return getActualType((ClassOrInterfaceType) type);
		} else if (type instanceof ReferenceType) {
			Type referenceType = ((ReferenceType) type).getType();
			String name;
			if (referenceType instanceof ReferenceType) {
				name = getActualType(referenceType);
			} else if (referenceType instanceof ClassOrInterfaceType) {
				name = getActualType((ClassOrInterfaceType) referenceType);
			} else if (referenceType instanceof PrimitiveType) {
				name = ((PrimitiveType) referenceType).getType().name();
			} else {
				throw new RuntimeException(referenceType.getClass().getName());
			}
			boolean isArray = ((ReferenceType) type).getArrayCount() != 0;
			if (isArray) {
				name += "[]";
			}
			return name;
		} else if (type instanceof PrimitiveType) {
			return ((PrimitiveType) type).getType().name();
		} else if (type instanceof VoidType) {
			return type.toString();
		} else {
			throw new RuntimeException(type.getClass().getName());
		}
	}

	/**
	 * Gets the actual type.
	 *
	 * @param type
	 *            the type
	 * @return the actual type
	 */
	private String getActualType(ClassOrInterfaceType type) {
		String name = "";
		ClassOrInterfaceType scope = type.getScope();
		if (scope != null) {
			name += getActualType(scope) + ".";
		}
		name += type.getName();
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.metricstream.walkmod.plugin.visitors.EqualsAndNotEqualsHandler#visit(org.walkmod.javalang.ast.
	 * CompilationUnit, org.walkmod.walkers.VisitorContext)
	 */
	@Override
	public void visit(CompilationUnit n, VisitorContext arg) {
		this.cu = n;
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.EnumDeclaration,
	 * java.lang.Object)
	 */
	public void visit(EnumDeclaration n, VisitorContext arg) {
		classStack.push(n.getName());
		variablesMap.put(classStack.peek(), new HashMap<String, Object>());
		super.visit(n, arg);
		variablesMap.remove(classStack.pop());
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
		classStack.push(n.getName());
		variablesMap.put(classStack.peek(), new HashMap<String, Object>());
		super.visit(n, arg);
		variablesMap.remove(classStack.pop());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.FieldDeclaration,
	 * java.lang.Object)
	 */
	@Override
	public void visit(FieldDeclaration n, VisitorContext arg) {
		Map<String, Object> m = variablesMap.get(classStack.peek());
		String type = getActualType(n.getType());
		List<VariableDeclarator> lDeclarators;
		// if(n instanceof VariableDeclarationExpr){
		// lDeclarators = ((VariableDeclarationExpr) n).getVars();
		// }
		// else{
		// }
		lDeclarators = n.getVariables();
		for (VariableDeclarator declarator : lDeclarators) {
			m.put(declarator.getId().getName(), type);
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
	@SuppressWarnings("unchecked")
	@Override
	public void visit(VariableDeclarationExpr n, VisitorContext arg) {
		Map<String, Object> m = variablesMap.get(classStack.peek());
		Object[] strings = methodStack.toArray();
		for (int i = 0; i < strings.length; i++) {
			m = (Map<String, Object>) m.get(strings[i]);
		}
		String type = getActualType(n.getType());
		List<VariableDeclarator> lDeclarators = n.getVars();
		for (VariableDeclarator declarator : lDeclarators) {
			m.put(declarator.getId().getName(), type);
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.ConstructorDeclaration,
	 * java.lang.Object)
	 */
	@Override
	public void visit(ConstructorDeclaration n, VisitorContext arg) {
		Map<String, Object> m = variablesMap.get(classStack.peek());
		String name = getUniqueMethodName(n);
		m.put(name, new HashMap<String, String>());
		methodStack.push(name);
		super.visit(n, arg);
		m.remove(methodStack.pop());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.MethodDeclaration,
	 * java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void visit(MethodDeclaration n, VisitorContext arg) {
		Map<String, Object> m = variablesMap.get(classStack.peek());
		if (!methodStack.isEmpty()) {
			Object[] strings = methodStack.toArray();
			for (int i = 0; i < methodStack.size(); i++) {
				if (m.containsKey(strings[i])) {
					m = (Map<String, Object>) m.get(strings[i]);
				}
			}
		}
		String name = getUniqueMethodName(n);
		Map<String, String> temp = new HashMap<String, String>();
		temp.put("this", getActualType(n.getType()));
		m.put(name, temp);
		methodStack.push(name);
		super.visit(n, arg);
		m.remove(methodStack.pop());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.body.Parameter,
	 * java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void visit(Parameter n, VisitorContext arg) {
		Map<String, Object> m = variablesMap.get(classStack.peek());
		Object[] strings = methodStack.toArray();
		for (int i = 0; i < methodStack.size(); i++) {
			m = (Map<String, Object>) m.get(strings[i]);
		}
		m.put(n.getId().getName(), getActualType(n.getType()));
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.EnclosedExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(EnclosedExpr n, VisitorContext arg) {
		Expression e = n.getInner();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setInner(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.BinaryExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(BinaryExpr n, VisitorContext arg) {
		Expression e = n.getLeft();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setLeft(expression);
			}
		}
		e = n.getRight();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setRight(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.WhileStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(WhileStmt n, VisitorContext arg) {
		Expression e = n.getCondition();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.DoStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(DoStmt n, VisitorContext arg) {
		Expression e = n.getCondition();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.ForStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(ForStmt n, VisitorContext arg) {
		Expression e = n.getCompare();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setCompare(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.IfStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(IfStmt n, VisitorContext arg) {
		Expression e = n.getCondition();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		super.visit(n, arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.expr.ConditionalExpr,
	 * java.lang.Object)
	 */
	@Override
	public void visit(ConditionalExpr n, VisitorContext arg) {
		Expression e = n.getCondition();
		if (e instanceof BinaryExpr) {
			Expression expression = getMethodCallExpr((BinaryExpr) e);
			if (expression != null) {
				n.setCondition(expression);
			}
		}
		super.visit(n, arg);
	}

	static {
		String propertyValues = PropertyFileReader.getPropertyValue("java.classes", "java.util");
		String[] classes = propertyValues.split(",");
		collectionLibraries = Arrays.asList(classes);
	}
}
