package org.walkmod.sonar.visitors;

import java.util.List;

import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * http://www.oracle.com/technetwork/java/codeconventions-135099.html
 * 
 * Variables ========= Except for variables, all instance, class, and class constants are in mixed
 * case with a lowercase first letter. Internal words start with capital letters. Variable names
 * should not start with underscore _ or dollar sign $ characters, even though both are allowed.
 * 
 * Variable names should be short yet meaningful. The choice of a variable name should be mnemonic-
 * that is, designed to indicate to the casual observer the intent of its use. One-character
 * variable names should be avoided except for temporary "throwaway" variables. Common names for
 * temporary variables are i, j, k, m, and n for integers; c, d, and e for characters.
 * 
 * @author rpau
 *
 */
@RequiresSemanticAnalysis
public class LocalVarsShouldComplyWithNamingConvention extends VoidVisitorAdapter<VisitorContext> {

   private boolean refactorParameters = true;

   private boolean refactorVariables = true;

   private boolean refactorFields = true;

   private boolean isValidVariableName(String name) {
      if (name == null || name.length() == 0) {
         return false;
      }
      char initial = name.charAt(0);
      if (Character.isLetter(initial) && Character.isLowerCase(initial)) {
         if (name.contains("_") || name.contains("\\$")) {
            return false;
         } else {
            return true;
         }
      }
      return false;
   }

   private String getValidNewName(String name) {
      char[] letters = name.toCharArray();
      String result = "";
      boolean toUpperCase = false;
      for (int i = 0; i < letters.length; i++) {
         if (letters[i] == '$' || letters[i] == '_') {
            i++;
            toUpperCase = true;
         } else {
            if (toUpperCase) {
               result += Character.toUpperCase(letters[i]);
               toUpperCase = false;
            } else {
               result += Character.toLowerCase(letters[i]);
            }
         }
      }
      return result;
   }

   @Override
   public void visit(VariableDeclarationExpr n, VisitorContext ctx) {
      if (refactorVariables) {
         List<VariableDeclarator> vars = n.getVars();
         if (vars != null) {
            for (VariableDeclarator var : vars) {
               String name = var.getId().getName();
               if (!isValidVariableName(name)) {
                  var.rename(getValidNewName(name));
               }
            }
         }
      }
      super.visit(n, ctx);
   }

   @Override
   public void visit(Parameter n, VisitorContext ctx) {
      if (refactorParameters) {
         String name = n.getId().getName();
         if (!isValidVariableName(name)) {
            n.rename(getValidNewName(name));
         }
      }
      super.visit(n, ctx);
   }

   @Override
   public void visit(FieldDeclaration n, VisitorContext ctx) {
      if (refactorFields) {
         if (ModifierSet.isPrivate(n.getModifiers())) {
            List<VariableDeclarator> vars = n.getVariables();
            if (vars != null) {
               for (VariableDeclarator var : vars) {
                  String name = var.getId().getName();
                  if (!isValidVariableName(name)) {
                     var.rename(getValidNewName(name));
                  }
               }
            }
         }
      }
      super.visit(n, ctx);
   }

   public boolean isRefactorParameters() {
      return refactorParameters;
   }

   public void setRefactorParameters(boolean refactorParameters) {
      this.refactorParameters = refactorParameters;
   }

   public boolean isRefactorVariables() {
      return refactorVariables;
   }

   public void setRefactorVariables(boolean refactorVariables) {
      this.refactorVariables = refactorVariables;
   }

   public boolean isRefactorFields() {
      return refactorFields;
   }

   public void setRefactorFields(boolean refactorFields) {
      this.refactorFields = refactorFields;
   }

}
