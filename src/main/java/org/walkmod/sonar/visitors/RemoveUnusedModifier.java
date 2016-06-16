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

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * Fields in interfaces are automatically public static final, and methods are public abstract.
 * Classes or interfaces nested in an interface are automatically public and static (all nested
 * interfaces are automatically static). For historical reasons, modifiers which are implied by the
 * context are accepted by the compiler, but are superfluous.
 * 
 * @author rpau
 *
 */
public class RemoveUnusedModifier extends VoidVisitorAdapter<VisitorContext>{

   @Override
   public void visit(FieldDeclaration n, VisitorContext ctx){
      super.visit(n, ctx);
      int modifiers = n.getModifiers();
      Node parent = n.getParentNode();
      
      if(parent != null && parent instanceof ClassOrInterfaceDeclaration){
         ClassOrInterfaceDeclaration type = (ClassOrInterfaceDeclaration) parent;
         
         if(type.isInterface()){
            if(ModifierSet.isPublic(modifiers)){
               modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PUBLIC);
            }
            if(ModifierSet.isStatic(modifiers)){
               modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.STATIC);
            }
            if(ModifierSet.isFinal(modifiers)){
               modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.FINAL);
            }
            n.setModifiers(modifiers);
         }
      }
   }
   
   @Override
   public void visit(MethodDeclaration n, VisitorContext ctx){
      super.visit(n, ctx);
      int modifiers = n.getModifiers();
      Node parent = n.getParentNode();
      
      if(parent != null && parent instanceof ClassOrInterfaceDeclaration){
         ClassOrInterfaceDeclaration type = (ClassOrInterfaceDeclaration) parent;
         
         if(type.isInterface()){
            if(ModifierSet.isPublic(modifiers)){
               modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PUBLIC);
            }
            if(ModifierSet.isAbstract(modifiers)){
               modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.ABSTRACT);
            }
            
            n.setModifiers(modifiers);
         }
      }
   }
   
   @Override
   public void visit(ClassOrInterfaceDeclaration n, VisitorContext ctx){
      super.visit(n, ctx);
      int modifiers = n.getModifiers();
      Node parent = n.getParentNode();
      
      if(parent != null && parent instanceof ClassOrInterfaceDeclaration){
         ClassOrInterfaceDeclaration type = (ClassOrInterfaceDeclaration) parent;
         
         if(type.isInterface()){
            if(ModifierSet.isPublic(modifiers)){
               modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.PUBLIC);
            }
            if(ModifierSet.isStatic(modifiers)){
               modifiers = ModifierSet.removeModifier(modifiers, ModifierSet.STATIC);
            }
            n.setModifiers(modifiers);
         }
      }
   }
}
