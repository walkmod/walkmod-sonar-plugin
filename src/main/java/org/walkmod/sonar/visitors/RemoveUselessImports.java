package org.walkmod.sonar.visitors;

import java.util.List;

import org.walkmod.javalang.ast.ImportDeclaration;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class RemoveUselessImports extends VoidVisitorAdapter<VisitorContext> {

   @Override
   public void visit(ImportDeclaration id, VisitorContext ctx) {
      List<SymbolReference> references = id.getUsages();
      if (!id.isNewNode()) {
         if (references == null || references.isEmpty()) {
            id.remove();
         } else {
            super.visit(id, ctx);
         }
      }
   }
}
