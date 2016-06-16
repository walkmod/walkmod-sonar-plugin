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

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.expr.AnnotationExpr;
import org.walkmod.javalang.ast.expr.AssignExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.SingleMemberAnnotationExpr;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class IgnoreJsonPropertyNonReadFields extends VoidVisitorAdapter<VisitorContext> {

   @Override
   public void visit(FieldDeclaration fd, VisitorContext ctx) {
      List<AnnotationExpr> ann = fd.getAnnotations();

      if (ann != null) {
         Iterator<AnnotationExpr> it = ann.iterator();

         while (it.hasNext()) {
            AnnotationExpr current = it.next();
            SymbolData sd = current.getSymbolData();

            if (sd.getName().equals("org.codehaus.jackson.annotate.JsonProperty")) {
               List<SymbolReference> sr = fd.getUsages();

               boolean addSupressWarnings = false;

               if (sr == null) {
                  // findbugs:URF_UNREAD_FIELD
                  addSupressWarnings = true;

               } else {
                  Iterator<SymbolReference> its = sr.iterator();
                  boolean finish = false;
                  while (its.hasNext() && !addSupressWarnings && !finish) {
                     SymbolReference next = its.next();
                     Node asNode = (Node) next;
                     Node parent = asNode.getParentNode();
                     if (parent instanceof AssignExpr) {
                        AssignExpr ae = (AssignExpr) parent;
                        addSupressWarnings = ae.getTarget() == asNode;
                     } else {
                        finish = true;
                     }
                  }

               }
               if (addSupressWarnings) {
                  Node parent = fd.getParentNode();

                  if (parent instanceof ClassOrInterfaceDeclaration) {
                     ClassOrInterfaceDeclaration type = (ClassOrInterfaceDeclaration) parent;

                     List<AnnotationExpr> ann2 = type.getAnnotations();

                     if (ann2 == null) {
                        ann2 = new LinkedList<AnnotationExpr>();
                        type.setAnnotations(ann2);
                     }
                     Iterator<AnnotationExpr> it2 = ann2.iterator();
                     boolean exists = false;
                     while (it2.hasNext() && !exists) {
                        AnnotationExpr currentAnn = it2.next();
                        exists = currentAnn.getName().getName().equals("SuppressWarnings");
                     }
                     if (!exists) {
                        ann2.add(new SingleMemberAnnotationExpr(new NameExpr("SuppressWarnings"),
                              new StringLiteralExpr("findbugs:URF_UNREAD_FIELD")));
                     }
                  }
               }
            }
         }
      }
   }
}
