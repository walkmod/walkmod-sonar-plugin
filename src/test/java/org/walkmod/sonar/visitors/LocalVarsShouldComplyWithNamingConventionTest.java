package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.AssignExpr;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.FieldAccessExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.test.SemanticTest;

public class LocalVarsShouldComplyWithNamingConventionTest extends SemanticTest {

   @Test
   public void testBasicReplace() throws Exception {
      CompilationUnit cu = compile("public class Foo{ private String name_; }");
      LocalVarsShouldComplyWithNamingConvention visitor = new LocalVarsShouldComplyWithNamingConvention();
      cu.accept(visitor, null);
      FieldDeclaration fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(0);

      String name = fd.getVariables().get(0).getId().getName();

      Assert.assertEquals("name", name);
   }

   @Test
   public void testBasicReplaceWithMax() throws Exception {
      CompilationUnit cu = compile("public class Foo{ private int Max; }");
      LocalVarsShouldComplyWithNamingConvention visitor = new LocalVarsShouldComplyWithNamingConvention();
      cu.accept(visitor, null);
      FieldDeclaration fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(0);

      String name = fd.getVariables().get(0).getId().getName();

      Assert.assertEquals("max", name);
   }

   @Test
   public void testBasicReplaceWithReferences() throws Exception {
      CompilationUnit cu = compile(
            "public class Foo{ private String name_; public void setName(String name_){ this.name_ = name_; }}");
      LocalVarsShouldComplyWithNamingConvention visitor = new LocalVarsShouldComplyWithNamingConvention();
      visitor.setRefactorParameters(false);
      cu.accept(visitor, null);
      FieldDeclaration fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(0);

      String name = fd.getVariables().get(0).getId().getName();

      Assert.assertEquals("name", name);

      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(1);
      ExpressionStmt stmt = (ExpressionStmt) md.getBody().getStmts().get(0);
      AssignExpr assign = (AssignExpr) stmt.getExpression();
      FieldAccessExpr target = (FieldAccessExpr) assign.getTarget(); //this.name_
      Assert.assertEquals("name", target.getField());

      NameExpr value = (NameExpr) assign.getValue();
      Assert.assertEquals("name_", value.getName());
   }

   @Test
   public void testBasicReplaceInConditions() throws Exception {
      CompilationUnit cu = compile(
            "public class Foo{ private String name_; public void setName(String x){ if(name_ != null) { name_=x;} }}");
      LocalVarsShouldComplyWithNamingConvention visitor = new LocalVarsShouldComplyWithNamingConvention();
      visitor.setRefactorParameters(false);
      cu.accept(visitor, null);
      FieldDeclaration fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(0);

      String name = fd.getVariables().get(0).getId().getName();

      Assert.assertEquals("name", name);

      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(1);
      IfStmt stmt = (IfStmt) md.getBody().getStmts().get(0);
      BinaryExpr assign = (BinaryExpr) stmt.getCondition();

      NameExpr value = (NameExpr) assign.getLeft();

      Assert.assertEquals("name", value.getName());
      
      System.out.println(cu);
   }
}
