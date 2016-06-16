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

import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.AnnotationExpr;
import org.walkmod.javalang.ast.expr.MarkerAnnotationExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

public class AddFunctionalInterface extends VoidVisitorAdapter<VisitorContext>{


   @Override
   public void visit(ClassOrInterfaceDeclaration n, VisitorContext ctx){
      super.visit(n, ctx);
      if(n.isInterface() && n.getExtends() == null){
         List<BodyDeclaration> members = n.getMembers();
         if(members != null && members.size() == 1){
            if(members.get(0) instanceof MethodDeclaration){
               List<AnnotationExpr> annotations = n.getAnnotations();
               if(annotations == null){
                  annotations = new LinkedList<AnnotationExpr>();
                  annotations.add(new MarkerAnnotationExpr(new NameExpr("FunctionalInterface")));
                  n.setAnnotations(annotations);
               }
               else{
                  boolean found = false;
                  Iterator<AnnotationExpr> it = annotations.iterator();
                  while(!found && it.hasNext()){
                     AnnotationExpr ann = it.next();
                     NameExpr name = ann.getName();
                     if(name.getName().equals("FunctionalInterface")){
                        found = true;
                     }
                  }
                  if(!found){
                     annotations.add(new MarkerAnnotationExpr(new NameExpr("FunctionalInterface")));
                  }
               }
            }
         }
      }
   }
}
