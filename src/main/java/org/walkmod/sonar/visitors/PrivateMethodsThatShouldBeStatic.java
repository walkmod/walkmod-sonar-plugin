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

import org.walkmod.javalang.ast.SymbolDefinition;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class PrivateMethodsThatShouldBeStatic extends VoidVisitorAdapter<VisitorContext>{

   @Override
   public void visit(MethodDeclaration md, VisitorContext ctx){
      super.visit(md, ctx);
      int modifiers = md.getModifiers();
      
      if(ModifierSet.isPrivate(modifiers)){
         List<SymbolReference> references = md.getBodyReferences();
         if(references != null){
            Iterator<SymbolReference> it = references.iterator();
            boolean canBeStatic = true;
            
            while(it.hasNext() && canBeStatic){
               SymbolReference sr = it.next();
               
               SymbolDefinition sd = sr.getSymbolDefinition();
               if(sd instanceof FieldDeclaration){
                  canBeStatic = false;
               }
               else if(sd instanceof VariableDeclarator){
                  VariableDeclarator vd = (VariableDeclarator) sd;
                  canBeStatic = !(vd.getParentNode() instanceof FieldDeclaration);
               }
               else if(sd instanceof MethodDeclaration){
                  MethodDeclaration method = (MethodDeclaration)sd;
                  canBeStatic = (!ModifierSet.isStatic(method.getModifiers()));
               }
            }
            
            if(canBeStatic){
               md.setModifiers(ModifierSet.addModifier(modifiers, ModifierSet.STATIC));
            }
         }
      }
   }
}
