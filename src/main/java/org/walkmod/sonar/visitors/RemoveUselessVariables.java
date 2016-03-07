package org.walkmod.sonar.visitors;

import java.util.List;

import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class RemoveUselessVariables extends VoidVisitorAdapter<VisitorContext> {

   @Override
   public void visit(VariableDeclarationExpr n, VisitorContext ctx) {

      List<VariableDeclarator> vars = n.getVars();

      if (vars != null) {

         for (VariableDeclarator var : vars) {
            if (var.getUsages() == null) {
               var.remove();
            }
         }
         vars = n.getVars();
         if (vars.isEmpty()) {
            n.remove();
            if(n.getParentNode() instanceof ExpressionStmt){
               n.getParentNode().remove();
            }
         }
      }
      super.visit(n, ctx);
      
     
   }

}
