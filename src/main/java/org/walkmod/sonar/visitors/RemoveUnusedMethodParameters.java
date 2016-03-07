package org.walkmod.sonar.visitors;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.walkmod.javalang.ast.MethodSymbolData;
import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitor;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.refactor.config.RefactorConfigurationController;
import org.walkmod.refactor.config.RefactoringUtils;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class RemoveUnusedMethodParameters extends VoidVisitorAdapter<VisitorContext> {

   private Map<String, String> refactoringRules = null;

   private Map<Method, VoidVisitor<?>> refactoringVisitors = null;

   public Map<String, String> getRefactoringRules() {
      return refactoringRules;
   }

   @Override
   public void visit(Parameter n, VisitorContext ctx) {
      SymbolData sd = n.getSymbolData();
      if (sd != null) {
         Node parent = n.getParentNode();
         if (parent != null && parent instanceof MethodDeclaration) {
            MethodDeclaration md = (MethodDeclaration) parent;
            MethodSymbolData methodDefinition = md.getSymbolData();
            if (methodDefinition != null) {

               List<SymbolReference> usages = n.getUsages();
               if (usages == null || usages.isEmpty()) {
                  if (!RefactoringUtils.overrides(md)) {
                     Method method = methodDefinition.getMethod();

                     String clazzName = method.getDeclaringClass().getName();
                     List<Parameter> params = md.getParameters();
                     String paramsString = "";
                     String varsString = "";
                     Iterator<Parameter> it = params.iterator();
                     boolean isValid = true;

                     while (it.hasNext() && isValid) {
                        Parameter p = it.next();
                        if (p != n) {
                           if (varsString.length() > 0) {
                              varsString += ", ";
                           }
                           varsString += p.getId().getName();
                        }
                        SymbolData sd_param = p.getSymbolData();
                        if (sd_param != null) {
                           if (paramsString.length() > 0) {
                              paramsString += ", ";
                           }
                           paramsString += sd_param.getClazz().getName() + " " + p.getId().getName();

                        } else {
                           isValid = false;
                        }
                     }
                     if (isValid) {

                        if (refactoringRules == null) {
                           RefactorConfigurationController controller = new RefactorConfigurationController();
                           refactoringRules = controller.getMethodRefactorRules(ctx);
                           refactoringVisitors = controller.getRefactoringVisitors(ctx);
                        }

                        refactoringRules.put(clazzName + ":" + method.getName() + "(" + paramsString + ")",
                              clazzName + ":" + method.getName() + "(" + varsString + ")");
                        refactoringVisitors.put(method, new ParameterRemover(n.getId().getName()));
                     }
                  }
               }
            }
         }
      }
   }

   private class ParameterRemover extends VoidVisitorAdapter<VisitorContext> {

      private String parameter;

      public ParameterRemover(String parameter) {
         this.parameter = parameter;
      }

      @Override
      public void visit(MethodDeclaration md, VisitorContext ctx) {
         List<Parameter> params = md.getParameters();
         if (params != null) {
            Iterator<Parameter> it = params.iterator();
            while (it.hasNext()) {
               Parameter param = it.next();
               if (param.getId().getName().equals(parameter)) {
                  param.remove();
               }
            }
         }
      }
   }
}
