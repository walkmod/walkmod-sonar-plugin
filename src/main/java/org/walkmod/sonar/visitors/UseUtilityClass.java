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
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.ConstructorDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.JavadocComment;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.expr.AnnotationExpr;
import org.walkmod.javalang.ast.expr.ArrayInitializerExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.SingleMemberAnnotationExpr;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class UseUtilityClass extends VoidVisitorAdapter<VisitorContext> {

  
   @Override
   public void visit(ClassOrInterfaceDeclaration n, VisitorContext ctx) {
      super.visit(n, ctx);
      if (!n.isInterface() && n.getExtends() == null) {

         List<AnnotationExpr> annotations = n.getAnnotations();
         boolean skipped = false;
         if (annotations != null) {
            Iterator<AnnotationExpr> it = annotations.iterator();

            while (it.hasNext() && !skipped) {
               AnnotationExpr ann = it.next();

               SymbolData sd = ann.getSymbolData();
               skipped = sd.getClazz().getName().equals("org.springframework.boot.autoconfigure.SpringBootApplication");
            }
         }
         if (skipped) {            
            List<Expression> exclusions = new LinkedList<Expression>();
            exclusions.add(new StringLiteralExpr("squid:S1118"));
            exclusions.add(new StringLiteralExpr("pmd:UseUtilityClass"));
            annotations.add(new SingleMemberAnnotationExpr(new NameExpr("SuppressWarnings"), new ArrayInitializerExpr(exclusions)));
         }
         else{
            List<BodyDeclaration> members = n.getMembers();
            if (members != null && !members.isEmpty()) {
               Iterator<BodyDeclaration> it = members.iterator();
               boolean areStatic = true;
               boolean containsMethods = false;
               int pos = 0;
               int index = 0;
               while (it.hasNext() && areStatic) {
                  BodyDeclaration next = it.next();
                  if (next instanceof MethodDeclaration) {
                     containsMethods = true;
                     MethodDeclaration md = (MethodDeclaration) next;
                     int modifiers = md.getModifiers();
                     areStatic = (ModifierSet.isStatic(modifiers));
                     if (index == 0) {
                        index = pos;
                     }
                  } else if (next instanceof FieldDeclaration) {
                     FieldDeclaration fd = (FieldDeclaration) next;
                     int modifiers = fd.getModifiers();
                     areStatic = (ModifierSet.isStatic(modifiers));
                  } else {
                     areStatic = false;
                  }
                  pos++;
               }
               if (areStatic && containsMethods) {

                  members.add(index,
                        new ConstructorDeclaration(new JavadocComment("Private constructor as an Utility class"),
                              ModifierSet.PRIVATE, null, null, n.getName(), null, null, new BlockStmt()));

               }

            }
         }
         //TODO: Requires full usage graph
      }
   }

}
