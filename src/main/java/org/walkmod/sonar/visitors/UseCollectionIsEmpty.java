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

import org.walkmod.javalang.ast.MethodSymbolData;
import org.walkmod.javalang.ast.expr.BinaryExpr;
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

   @Override
   public void visit(BinaryExpr n, VisitorContext ctx) {
      super.visit(n, ctx);
      if (isValid(n.getOperator())) {
         Expression left = n.getLeft();
         Expression right = n.getRight();

         Expression numberExpr = null;
         Expression methodExpr = null;

         if (left instanceof MethodCallExpr) {
            if (right instanceof IntegerLiteralExpr) {
               numberExpr = right;
               methodExpr = left;
            }
         } else if (left instanceof IntegerLiteralExpr) {
            if (right instanceof MethodCallExpr) {
               numberExpr = left;
               methodExpr = right;
            }
         }
         if (methodExpr != null) {
            MethodCallExpr mce = (MethodCallExpr) methodExpr;
            MethodSymbolData msd = mce.getSymbolData();
            if (msd != null) {
               if (mce.getName().equals("size") && ("0".equals(((IntegerLiteralExpr) numberExpr).getValue()))) {

                  if (Collection.class.isAssignableFrom(msd.getMethod().getDeclaringClass())) {
                     Expression newExpr = new MethodCallExpr(mce.getScope(), "isEmpty");
                     if (n.getOperator().equals(BinaryExpr.Operator.notEquals)) {
                        newExpr = new UnaryExpr(newExpr, UnaryExpr.Operator.not);
                     }

                     n.getParentNode().replaceChildNode(n, newExpr);
                  }
               } else if (n.getOperator().equals(BinaryExpr.Operator.greater) && mce.getName().equals("size")
                     && ("1".equals(((IntegerLiteralExpr) numberExpr).getValue()))) {

                  if (msd.getMethod().getDeclaringClass().isAssignableFrom(Collection.class)) {
                     n.getParentNode().replaceChildNode(n,
                           new UnaryExpr(new MethodCallExpr(mce.getScope(), "isEmpty"), UnaryExpr.Operator.not));
                  }
               }
            }
         }
      }
   }

   private boolean isValid(BinaryExpr.Operator op) {
      return op.equals(BinaryExpr.Operator.equals) || op.equals(BinaryExpr.Operator.notEquals)
            || op.equals(BinaryExpr.Operator.greater) || op.equals(BinaryExpr.Operator.less);
   }

}
