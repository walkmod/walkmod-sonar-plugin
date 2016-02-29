package org.walkmod.sonar.visitors;

import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.expr.CastExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class RedundantCastsShouldNotBeUsed extends VoidVisitorAdapter<VisitorContext> {

   @Override
   public void visit(CastExpr n, VisitorContext ctx) {

      Type type = n.getType();
      SymbolData sd = type.getSymbolData();
      if (sd != null) {
         Expression expr = n.getExpr();
         SymbolData exprSymbolData = expr.getSymbolData();
         if (exprSymbolData.equals(sd)) {
            n.getParentNode().replaceChildNode(n, expr);
         }
      }
      super.visit(n, ctx);
   }
}
