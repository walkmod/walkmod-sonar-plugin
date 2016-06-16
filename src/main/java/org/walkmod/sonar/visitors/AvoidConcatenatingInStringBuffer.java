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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class AvoidConcatenatingInStringBuffer extends VoidVisitorAdapter<VisitorContext> {

   @Override
   public void visit(MethodCallExpr n, VisitorContext ctx) {
      super.visit(n, ctx);
      if ("append".equals(n.getName())) {
         Expression scope = n.getScope();
         if (scope != null) {
            SymbolData sd = scope.getSymbolData();
            if (sd != null) {
               if (sd.getClazz().getName().equals("java.lang.StringBuffer")) {
                  List<Expression> expr = n.getArgs();
                  if (expr != null && expr.size() == 1) {
                     Expression arg = expr.get(0);
                     if (arg instanceof BinaryExpr) {
                        BinaryExpr be = (BinaryExpr) arg;
                        SymbolData sdbe = be.getSymbolData();
                        if (sdbe != null && "java.lang.String".equals(sdbe.getClazz().getName())) {
                           if (be.getOperator().equals(BinaryExpr.Operator.plus)) {
                              List<Expression> newArgs = decompose(be);
                              Expression newscope = n.getScope();
                              Iterator<Expression> it = newArgs.iterator();
                              while (it.hasNext()) {
                                 Expression e = it.next();
                                 List<Expression> literals = new LinkedList<Expression>();
                                 literals.add(e);
                                 MethodCallExpr newCall = new MethodCallExpr(newscope, "append", literals);
                                 if (!it.hasNext()) {
                                    n.getParentNode().replaceChildNode(n, newCall);

                                 } else {
                                    newscope = newCall;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private List<Expression> decompose(BinaryExpr be) {
      List<Expression> result = new LinkedList<Expression>();

      SymbolData sdbe = be.getSymbolData();
      if (sdbe != null && "java.lang.String".equals(sdbe.getClazz().getName())) {
         if (be.getOperator().equals(BinaryExpr.Operator.plus)) {
            Expression left = be.getLeft();
            if (left instanceof BinaryExpr) {
               result.addAll(decompose((BinaryExpr) left));
            } else {
               result.add(left);
            }
            Expression right = be.getRight();
            if (right instanceof BinaryExpr) {
               result.addAll(decompose((BinaryExpr) right));
            } else {
               result.add(right);
            }
         }

      }
      return result;
   }
}
