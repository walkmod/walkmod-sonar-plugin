/* 
  Copyright (C) 2016 Raquel Pau.
 
  Walkmod is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  Walkmod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/

package org.walkmod.sonar.visitors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.walkmod.javalang.ast.MethodSymbolData;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.BinaryExpr.Operator;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.IntegerLiteralExpr;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
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
 * @author rpau
 * 
 */
@RequiresSemanticAnalysis
public class UseCollectionIsEmpty extends VoidVisitorAdapter<VisitorContext> {
	
	private static final String IS_EMPTY = "isEmpty";
	private static Set<String> isEmptyConditionSet = prepareIsEmptyConditionSet();
	private static Set<String> isNotEmptyConditionSet = prepareIsNotEmptyConditionSet();
	private static final String SIZE = "size";
	private static final String ZERO = "0";
	private static final String ONE = "1";

   @Override
   public void visit(BinaryExpr n, VisitorContext ctx) {
      super.visit(n, ctx);
      if (isValid(n.getOperator())) {
         Expression left = n.getLeft();
         Expression right = n.getRight();
         MethodCallExpr mce = getMethodCallExpression(left, right);
         if (mce != null) {
             MethodSymbolData msd = mce.getSymbolData();
             if (Collection.class.isAssignableFrom(msd.getMethod().getDeclaringClass())) {
            	 if(isEmptyCondition(left, n.getOperator(), right)){
                	 Expression newExpr = new MethodCallExpr(mce.getScope(), IS_EMPTY);
                     n.getParentNode().replaceChildNode(n, newExpr);
                 } else if (isNotEmptyCondition(left, n.getOperator(), right)){
                	 Expression newExpr = new MethodCallExpr(mce.getScope(), IS_EMPTY);
                	 newExpr = new UnaryExpr(newExpr, UnaryExpr.Operator.not);

                     n.getParentNode().replaceChildNode(n, newExpr);
                 }
             }
         }
      }
   }

   private MethodCallExpr getMethodCallExpression(Expression left, Expression right) {
	   MethodCallExpr methodExpr = null;

       if (left instanceof MethodCallExpr) {
    	   methodExpr = (MethodCallExpr)left;
       } else if (right instanceof MethodCallExpr) {
           methodExpr = (MethodCallExpr)right;
       }
       
       return methodExpr;
   }

private boolean isNotEmptyCondition(Expression left, Operator operator,
		Expression right) {
	return isNotEmptyConditionSet.contains(newCondition(getExpression(left), operator, getExpression(right)));
}

private boolean isEmptyCondition(Expression left, Operator operator,
		Expression right) {
	return isEmptyConditionSet.contains(newCondition(getExpression(left), operator, getExpression(right)));
}

private String getExpression(Expression expression) {
    String expStr = null;

    if (expression instanceof MethodCallExpr) {
    	expStr = ((MethodCallExpr)expression).getName();
    } else if (expression instanceof IntegerLiteralExpr) {
    	expStr = ((IntegerLiteralExpr)expression).getValue();
    }
	return expStr;
}

private static Set<String> prepareIsEmptyConditionSet() {
	   Set<String> isEmptyConditionSet = new HashSet<String>();
	   isEmptyConditionSet.add(newCondition(SIZE, BinaryExpr.Operator.equals, ZERO));
	   isEmptyConditionSet.add(newCondition(ZERO, BinaryExpr.Operator.equals, SIZE));
	   isEmptyConditionSet.add(newCondition(SIZE, BinaryExpr.Operator.lessEquals, ZERO));
	   isEmptyConditionSet.add(newCondition(ZERO, BinaryExpr.Operator.greaterEquals, SIZE));
	   isEmptyConditionSet.add(newCondition(SIZE, BinaryExpr.Operator.less, ONE));
	   isEmptyConditionSet.add(newCondition(ONE, BinaryExpr.Operator.greater, SIZE));
	   return isEmptyConditionSet;
   }
   
   public static String newCondition(String leftExp, BinaryExpr.Operator operator, String rightExp) {
	   StringBuilder condBuilder = new StringBuilder();
	   condBuilder.append(String.valueOf(leftExp));
	   condBuilder.append(String.valueOf(operator));
	   condBuilder.append(String.valueOf(rightExp));
	   return condBuilder.toString();
   }

   private static Set<String> prepareIsNotEmptyConditionSet() {
	   Set<String> isNotEmptyConditionSet = new HashSet<String>();
	   isNotEmptyConditionSet.add(newCondition(SIZE, BinaryExpr.Operator.notEquals, ZERO));
	   isNotEmptyConditionSet.add(newCondition(ZERO, BinaryExpr.Operator.notEquals, SIZE));
	   isNotEmptyConditionSet.add(newCondition(SIZE, BinaryExpr.Operator.greater, ZERO));
	   isNotEmptyConditionSet.add(newCondition(ZERO, BinaryExpr.Operator.less, SIZE));
	   isNotEmptyConditionSet.add(newCondition(SIZE, BinaryExpr.Operator.greaterEquals, ONE));
	   isNotEmptyConditionSet.add(newCondition(ONE, BinaryExpr.Operator.lessEquals, SIZE));
	   return isNotEmptyConditionSet;
   }

private boolean isValid(BinaryExpr.Operator op) {
      return op.equals(BinaryExpr.Operator.equals) || op.equals(BinaryExpr.Operator.notEquals)
            || op.equals(BinaryExpr.Operator.greater) || op.equals(BinaryExpr.Operator.greaterEquals)
            || op.equals(BinaryExpr.Operator.less) || op.equals(BinaryExpr.Operator.lessEquals);
   }
}
