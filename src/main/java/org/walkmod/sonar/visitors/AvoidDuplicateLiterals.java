package org.walkmod.sonar.visitors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.walkmod.javalang.ast.SymbolDefinition;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class AvoidDuplicateLiterals extends VoidVisitorAdapter<VisitorContext> {

   private Map<String, List<StringLiteralExpr>> literals = new HashMap<String, List<StringLiteralExpr>>();

   @Override
   public void visit(StringLiteralExpr n, VisitorContext ctx) {
      List<StringLiteralExpr> instances = literals.get(n.getValue());

      if (instances == null) {
         instances = new LinkedList<StringLiteralExpr>();
         literals.put(n.getValue(), instances);
      }
      literals.put(n.getValue(), instances);

   }

   @Override
   public void visit(ClassOrInterfaceDeclaration n, VisitorContext ctx) {
      Map<String, List<StringLiteralExpr>> oldLiterals = literals;
      literals = new HashMap<String, List<StringLiteralExpr>>();
      super.visit(n, ctx);

      Set<String> keys = literals.keySet();
      if (!keys.isEmpty()) {

         Map<String, SymbolDefinition> vars = n.getVariableDefinitions();
         for (String key : keys) {
            List<StringLiteralExpr> instances = literals.get(key);
            String varName = key.toUpperCase() + "_CONSTANT";
            if (instances.size() > 1 && !vars.containsKey(varName)) {

               FieldDeclaration fd = new FieldDeclaration(ModifierSet.PRIVATE, new ClassOrInterfaceType("String"),
                     new VariableDeclarator(new VariableDeclaratorId(varName)));
               n.getMembers().add(fd);
            }
         }
      }
      literals = oldLiterals;
   }

}
