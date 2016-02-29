package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.stmt.ReturnStmt;
import org.walkmod.javalang.test.SemanticTest;

public class RedundantCastsShouldNotBeUsedTest extends SemanticTest{

   @Test
   public void testSimpleType() throws Exception{
      
      CompilationUnit cu = compile("public class Foo{ public Integer bar(Integer x) { return (Integer) x; } }");
      RedundantCastsShouldNotBeUsed visitor = new RedundantCastsShouldNotBeUsed();
      cu.accept(visitor, null);
      
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      
      ReturnStmt returnStmt = (ReturnStmt)md.getBody().getStmts().get(0);
      Assert.assertTrue(returnStmt.getExpr() instanceof NameExpr);
   }
   
   @Test
   public void testParameterizedType() throws Exception{
      
      CompilationUnit cu = compile("import java.util.List; public class Foo{ public List<Integer> bar(List<Integer> x) { return (List<Integer>) x; } }");
      RedundantCastsShouldNotBeUsed visitor = new RedundantCastsShouldNotBeUsed();
      cu.accept(visitor, null);
      
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      
      ReturnStmt returnStmt = (ReturnStmt)md.getBody().getStmts().get(0);
      Assert.assertTrue(returnStmt.getExpr() instanceof NameExpr);
   }
}
