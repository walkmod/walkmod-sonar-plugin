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

import java.util.List;

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

public class CollapsibleIfStatements extends  VoidVisitorAdapter<VisitorContext> {

   @Override
   public void visit(IfStmt n, VisitorContext ctx) {
      Node parent = n.getParentNode();

      if (parent != null) {
         if (parent instanceof BlockStmt) {
            parent = parent.getParentNode();
         }
         if (parent instanceof IfStmt) {
            IfStmt parentIf = (IfStmt) parent;

            Statement elseStmt = parentIf.getElseStmt();
            if (elseStmt == null) {

               Statement thisElseStmt = n.getElseStmt();
               if (thisElseStmt == null) {
                  try{
                  Expression rightExpression = parentIf.getCondition();
                  if (rightExpression instanceof BinaryExpr) {
                     rightExpression = new EnclosedExpr(parentIf.getCondition().clone());
                  }

                  Expression leftExpression = n.getCondition();
                  if (leftExpression instanceof BinaryExpr) {
                     leftExpression = new EnclosedExpr(n.getCondition().clone());
                  }

                  BinaryExpr condition = new BinaryExpr(rightExpression, leftExpression, BinaryExpr.Operator.and);

                  if (parentIf.getThenStmt() == n) {
                     parentIf.setThenStmt(n.getThenStmt().clone());
                     parentIf.setCondition(condition);
                  } else {
                     Statement stmt = parentIf.getThenStmt();
                     if (stmt instanceof BlockStmt) {
                        BlockStmt block = (BlockStmt) stmt;
                        List<Statement> stmts = block.getStmts();
                        if (stmts.size() == 1) {
                           parentIf.setThenStmt(n.getThenStmt().clone());
                           parentIf.setCondition(condition);
                        }
                     }
                  }
                  }catch(CloneNotSupportedException e){
                     throw new RuntimeException(e);
                  }
               }
            }
         }
      }
      super.visit(n, ctx);
   }
}
