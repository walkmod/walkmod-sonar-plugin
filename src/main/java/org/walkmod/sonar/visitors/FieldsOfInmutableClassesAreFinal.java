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

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.expr.AnnotationExpr;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class FieldsOfInmutableClassesAreFinal extends VoidVisitorAdapter<VisitorContext> {

   public void visit(ClassOrInterfaceDeclaration n, VisitorContext ctx) {
      super.visit(n, ctx);
      List<AnnotationExpr> annotations = n.getAnnotations();
      if (annotations != null) {
         Iterator<AnnotationExpr> it = annotations.iterator();
         while (it.hasNext()) {
            AnnotationExpr ae = it.next();
            SymbolData sd = ae.getSymbolData();
            if (sd != null) {
               if (sd.getClazz().getName().equals("javax.annotation.concurrent.Immutable")
                     || sd.getClazz().getName().equals("net.jcip.annotations.Immutable")) {
                  List<BodyDeclaration> members = n.getMembers();

                  if (members != null) {
                     Iterator<BodyDeclaration> itm = members.iterator();

                     while (itm.hasNext()) {
                        BodyDeclaration member = itm.next();

                        if (member instanceof FieldDeclaration) {
                           FieldDeclaration fd = (FieldDeclaration) member;

                           int modifiers = fd.getModifiers();

                           if (!ModifierSet.isFinal(modifiers)) {
                              
                              ((FieldDeclaration) member)
                                    .setModifiers(ModifierSet.addModifier(modifiers, Modifier.FINAL));
                           }
                        }
                     }
                  }
               }
            }

         }
      }
   }
}
