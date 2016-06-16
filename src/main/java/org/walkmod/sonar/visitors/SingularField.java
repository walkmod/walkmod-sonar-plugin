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
import java.util.List;

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.AnnotationExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/*
 * A field that's only used by one method could perhaps be replaced by a local variable.*/
@RequiresSemanticAnalysis
public class SingularField extends VoidVisitorAdapter<VisitorContext> {

   @Override
   public void visit(FieldDeclaration fd, VisitorContext ctx) {
      List<AnnotationExpr> annotations = fd.getAnnotations();
      boolean isNotInjectable = (annotations == null || annotations.isEmpty());
      int modifiers = fd.getModifiers();
      SymbolData sd = fd.getType().getSymbolData();

      if (isNotInjectable && ModifierSet.isPrivate(modifiers) && !ModifierSet.isStatic(modifiers)
            && (sd != null && !sd.getName().endsWith("Logger"))) {

         List<VariableDeclarator> vars = fd.getVariables();
         boolean isInit = false;
         if (vars != null) {
            Iterator<VariableDeclarator> it = vars.iterator();

            while (it.hasNext() && !isInit) {
               VariableDeclarator vd = it.next();
               Expression init = vd.getInit();
               boolean isValid = true;
               
               if (init instanceof MethodCallExpr) {
                  MethodCallExpr mce = (MethodCallExpr) init;
                  isValid = !(mce.getName().equals("currentTimeMillis"));
               }
               isInit = init != null && isValid;
            }
         }
         if (isInit) {
            List<SymbolReference> refs = fd.getUsages();
            if (refs != null) {
               MethodDeclaration md = null;
               Iterator<SymbolReference> it = refs.iterator();
               boolean isSingular = true;
               while (it.hasNext() && isSingular) {
                  SymbolReference sr = it.next();
                  MethodDeclaration aux = getMethod((Node) sr);
                  if (aux == null) {
                     isSingular = false;
                  } else if (md == null) {
                     md = aux;
                  } else if (md != aux) {
                     isSingular = false;
                  }
               }
               if (isSingular) {

                  MethodDeclaration aux = (MethodDeclaration) md;
                  BlockStmt block = aux.getBody();
                  List<Statement> stmts = block.getStmts();

                  try {
                     FieldDeclaration fdaux = fd.clone();
                     stmts.add(0,
                           new ExpressionStmt(new VariableDeclarationExpr(fdaux.getType(), fdaux.getVariables())));
                     fd.remove();
                  } catch (CloneNotSupportedException e) {
                     throw new RuntimeException(e);
                  }
               }
            }
         }
      }
   }

   public MethodDeclaration getMethod(Node n) {
      if (n == null) {
         return null;
      }

      if (n instanceof MethodDeclaration) {
         return (MethodDeclaration) n;
      } else {
         return getMethod(n.getParentNode());
      }
   }
   
  
}
