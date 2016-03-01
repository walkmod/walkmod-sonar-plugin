package org.walkmod.sonar.visitors;

import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

public class RemoveUselessParentheses extends VoidVisitorAdapter<VisitorContext>{

   @Override
   public void visit(EnclosedExpr n, VisitorContext ctx){
      Expression inner = n.getInner();
      if(!(inner instanceof BinaryExpr)){
         n.getParentNode().replaceChildNode(n, inner);
         inner.accept(this, ctx);
      }
      else{
         super.visit(n, ctx);
      }
   }
}
