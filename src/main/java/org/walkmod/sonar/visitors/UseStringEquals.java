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

import java.util.LinkedList;
import java.util.List;

import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.CharLiteralExpr;
import org.walkmod.javalang.ast.expr.DoubleLiteralExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.IntegerLiteralExpr;
import org.walkmod.javalang.ast.expr.LongLiteralExpr;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class UseStringEquals.
 *
 * @author mohanasundar.n
 */
public class UseStringEquals extends VoidVisitorAdapter<VisitorContext> {

   public void visit(BinaryExpr n, VisitorContext ctx) {
      if (n.getOperator().equals(BinaryExpr.Operator.equals)) {
         Expression left = n.getLeft();
         Expression right = n.getRight();
         if (isStringLiteralExpr(left)) {
            List<Expression> args = new LinkedList<Expression>();
            args.add(right);
            n.getParentNode().replaceChildNode(n, new MethodCallExpr(left, "equals", args));
         } else if (isStringLiteralExpr(right)) {
            List<Expression> args = new LinkedList<Expression>();
            args.add(left);
            n.getParentNode().replaceChildNode(n, new MethodCallExpr(right, "equals", args));
         }
      } else if (n.getOperator().equals(BinaryExpr.Operator.notEquals)) {
         Expression left = n.getLeft();
         Expression right = n.getRight();
         if (isStringLiteralExpr(left)) {
            List<Expression> args = new LinkedList<Expression>();
            args.add(right);
            n.getParentNode().replaceChildNode(n,
                  new UnaryExpr(new MethodCallExpr(left, "equals", args), UnaryExpr.Operator.not));
         } else if (isStringLiteralExpr(right)) {
            List<Expression> args = new LinkedList<Expression>();
            args.add(left);
            n.getParentNode().replaceChildNode(n,
                  new UnaryExpr(new MethodCallExpr(right, "equals", args), UnaryExpr.Operator.not));
         }
      }
      super.visit(n, ctx);
   }

   /**
    * Checks whether the given {@link Expression} is instance of {@link StringLiteralExpr}.
    *
    * @param e
    *           The instance of {@link Expression}
    * @return true, If is {@link StringLiteralExpr}
    */
   private boolean isStringLiteralExpr(Expression e) {
      boolean isString = e instanceof StringLiteralExpr;
      isString = isString && !(e instanceof CharLiteralExpr);
      isString = isString && !(e instanceof DoubleLiteralExpr);
      isString = isString && !(e instanceof IntegerLiteralExpr);
      isString = isString && !(e instanceof LongLiteralExpr);
      return isString;
   }
}
