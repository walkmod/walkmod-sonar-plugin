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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.ImportDeclaration;
import org.walkmod.javalang.ast.MethodSymbolData;
import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.SymbolDataAware;
import org.walkmod.javalang.ast.SymbolDefinition;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.AssignExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.ReturnStmt;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.ast.type.ReferenceType;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.compiler.symbols.SymbolType;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class DeclarationsShouldUseCollectionInterfaces extends VoidVisitorAdapter<VisitorContext> {

   private Set<String> pendingTypes = new HashSet<String>();

   @Override
   public void visit(VariableDeclarationExpr n, VisitorContext ctx) {
      SymbolData sd = n.getType().getSymbolData();
      if (sd != null && Collection.class.isAssignableFrom(sd.getClazz()) && !sd.getClazz().isInterface()) {
         visitVariableOrField(n, sd, n.getType(), n.getVars(), ctx);
      }
      super.visit(n, ctx);
   }

   private void visitVariableOrField(Node n, SymbolData sd, Type type, List<VariableDeclarator> vars,
         VisitorContext ctx) {
      if (vars != null) {
         Class<?> clazz = null;
         boolean refactor = true;
         for (VariableDeclarator var : vars) {
            List<SymbolReference> usages = var.getUsages();

            if (usages != null) {
               GetGenericCollectionClass visitor = new GetGenericCollectionClass();

               Iterator<SymbolReference> itRef = usages.iterator();

               while (itRef.hasNext() && refactor) {
                  SymbolReference usage = itRef.next();
                  ((Node) usage).accept(visitor, ctx);
                  refactor = visitor.isRefactorizable();
               }
               if (refactor) {
                  if (clazz == null) {
                     clazz = visitor.getDeclaringClass();
                  } else if (!clazz.isAssignableFrom(visitor.getDeclaringClass())) {
                     clazz = visitor.getDeclaringClass();
                  }
               }

            }
         }
         if (refactor) {

            if (clazz == null) {
               if (!sd.getClazz().isInterface()) {
                  Class<?>[] interfaces = sd.getClazz().getInterfaces();
                  for (int i = 0; i < interfaces.length && clazz == null; i++) {
                     if (Collection.class.isAssignableFrom(interfaces[i])) {
                        clazz = interfaces[i];
                     }
                  }
               }
            }
            if (clazz != null) {
               if (clazz.isAssignableFrom(sd.getClazz()) && !sd.getClazz().isAssignableFrom(clazz)) {
                  ClassOrInterfaceType aux = new ClassOrInterfaceType(clazz.getSimpleName());
                  GetTypeArgs resolver = new GetTypeArgs();
                  type.accept(resolver, ctx);
                  aux.setTypeArgs(resolver.typeArgs());
                  aux.setSymbolData(new SymbolType(clazz));
                  n.replaceChildNode(type, aux);
                  pendingTypes.add(clazz.getName());
               }
            }
         }

      }
   }

   @Override
   public void visit(FieldDeclaration n, VisitorContext ctx) {
      SymbolData sd = n.getType().getSymbolData();
      if (sd != null && Collection.class.isAssignableFrom(sd.getClazz()) && !sd.getClazz().isInterface()) {
         visitVariableOrField(n, sd, n.getType(), n.getVariables(), ctx);
      }
      super.visit(n, ctx);
   }

   @Override
   public void visit(CompilationUnit n, VisitorContext ctx) {
      pendingTypes.clear();
      super.visit(n, ctx);
      if (!pendingTypes.isEmpty()) {
         boolean add = true;

         List<ImportDeclaration> imports = n.getImports();
         for (String type : pendingTypes) {
            if (imports != null) {
               Iterator<ImportDeclaration> it = imports.iterator();
               boolean exists = false;
               while (it.hasNext() && !exists) {
                  ImportDeclaration id = it.next();
                  if (!id.isAsterisk()) {
                     String typeName = id.getName().toString();
                     if (typeName.equals(type)) {
                        exists = true;
                     }
                  } else {
                     List<SymbolReference> references = id.getUsages();
                     if (references != null) {
                        Iterator<SymbolReference> itRef = references.iterator();

                        while (itRef.hasNext() && !exists) {
                           SymbolReference ref = itRef.next();
                           if (ref instanceof SymbolDataAware) {
                              SymbolDataAware<?> sda = (SymbolDataAware) ref;
                              SymbolData sd = sda.getSymbolData();
                              if (sd != null) {
                                 if (sd.getName().equals(type)) {
                                    exists = true;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
               add = !exists;
            }
            if (add) {
               imports.add(new ImportDeclaration(new NameExpr(type), false, false));
            }
         }
      }

   }
   
   private class GetTypeArgs extends VoidVisitorAdapter<VisitorContext>{
      
      private List<Type> typeArgs = null;
      
      @Override
      public void visit(ClassOrInterfaceType type, VisitorContext ctx){
         typeArgs = type.getTypeArgs();
      }
      
      public List<Type> typeArgs(){
         return typeArgs;
      }
   }

   private class GetGenericCollectionClass extends VoidVisitorAdapter<VisitorContext> {

      private Class<?> declaringClass = null;

      private boolean refactorizable = true;

      @Override
      public void visit(MethodCallExpr n, VisitorContext ctx) {
         MethodSymbolData msd = n.getSymbolData();
         if (msd != null) {
            Class<?> clazz = msd.getMethod().getDeclaringClass();
            if (declaringClass == null) {
               declaringClass = clazz;
            } else {
               if (!declaringClass.isAssignableFrom(clazz) && clazz.isAssignableFrom(declaringClass)) {
                  declaringClass = clazz;
               }
            }
         } else {
            refactorizable = false;
         }
         super.visit(n, ctx);
      }

      public void visit(NameExpr n, VisitorContext ctx) {
         super.visit(n, ctx);
         Node parent = n.getParentNode();
         if (parent instanceof ReturnStmt) {
            Node methodDeclaration = n.getParentNode();
            while (!(methodDeclaration instanceof MethodDeclaration)) {
               methodDeclaration = methodDeclaration.getParentNode();
            }
            MethodDeclaration md = (MethodDeclaration) methodDeclaration;
            SymbolData sd = md.getType().getSymbolData();
            if (sd != null) {
               Class<?> clazz = sd.getClazz();
               if (declaringClass == null) {
                  declaringClass = clazz;
               } else {
                  if (!declaringClass.isAssignableFrom(clazz) && clazz.isAssignableFrom(declaringClass)) {
                     declaringClass = clazz;
                  }
               }
            } else {
               refactorizable = false;
            }
         } else if (parent instanceof AssignExpr) {
            AssignExpr assign = (AssignExpr) parent;
            if (assign.getValue() == n) {
               Expression target = assign.getTarget();
               if (target instanceof SymbolReference) {
                  SymbolReference sr = (SymbolReference) target;
                  SymbolDefinition sd = sr.getSymbolDefinition();
                  if (sd instanceof SymbolDataAware) {

                     DeclarationsShouldUseCollectionInterfaces auxVisitor = new DeclarationsShouldUseCollectionInterfaces();
                     ((Node) sd).accept(auxVisitor, ctx);
                     SymbolDataAware<?> sda = (SymbolDataAware) sd;
                     SymbolData computedSd = sda.getSymbolData();
                     if (computedSd != null) {
                        Class<?> clazz = computedSd.getClazz();
                        if (declaringClass == null) {
                           declaringClass = clazz;
                        } else {
                           if (!declaringClass.isAssignableFrom(clazz) && clazz.isAssignableFrom(declaringClass)) {
                              declaringClass = clazz;
                           }
                        }
                     } else {
                        refactorizable = false;
                     }
                  }
               }
            }
         }
      }

      public boolean isRefactorizable() {
         return refactorizable;
      }

      public Class<?> getDeclaringClass() {
         return declaringClass;
      }

   }
}
