package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.javalang.ast.stmt.ReturnStmt;


public class RemoveUselessParenthesesTest {

   @Test
   public void test() throws Exception{
      CompilationUnit cu = ASTManager.parse("public class Foo{ public int get() { return (5)*(4-3); } }");
      RemoveUselessParentheses visitor = new RemoveUselessParentheses();
      cu.accept(visitor, null);
      System.out.println(cu);
   }
   
   @Test
   public void test2() throws Exception{
      CompilationUnit cu = ASTManager.parse("public class Foo{ public int get() { String a = \"aaa\",b =  \"bbb\"; "
              + "if(((a!=null) || (b!=null && b.length()>0)))"
              + "return false;} }");
      RemoveUselessParentheses visitor = new RemoveUselessParentheses();
      cu.accept(visitor, null);
      System.out.println(cu);
   }
   
   @Test
   public void test3() throws Exception{
      CompilationUnit cu = ASTManager.parse("public class Foo{ public int get() { String a = \"aaa\",b =  \"bbb\"; "
              + "if((a!=null) || (b!=null && b.length()>0))"
              + "return false;} }");
      RemoveUselessParentheses visitor = new RemoveUselessParentheses();
      cu.accept(visitor, null);
      System.out.println(cu);
   }
   
   @Test
   public void testWithCastExpr() throws Exception{
      CompilationUnit cu = ASTManager.parse("public class Foo { private Object i; public boolean foo() { if (((Integer)i).intValue() == 0) { return true; }else{ return false;} } } ");
      RemoveUselessParentheses visitor = new RemoveUselessParentheses();
      cu.accept(visitor, null);
      System.out.println(cu);
   }
   
   @Test
   public void testWithLargeNegativeExpression() throws Exception{
     
      CompilationUnit cu = ASTManager.parse("public class Foo { public boolean eval( int yEnd1, int yOrigin2, int xEnd1, int xOrigin2, int yEnd2, int yOrigin1, int xEnd2, int xOrigin1) {"
            +"return !(((yEnd1 <= yOrigin2) || (xEnd1 <= xOrigin2)) || ((yEnd2 <= yOrigin1) || (xEnd2 <= xOrigin1)));"
            +"}}");
      RemoveUselessParentheses visitor = new RemoveUselessParentheses();
      cu.accept(visitor, null);
      MethodDeclaration md = (MethodDeclaration)cu.getTypes().get(0).getMembers().get(0);
      ReturnStmt stmt = (ReturnStmt) md.getBody().getStmts().get(0);
      
      Assert.assertTrue(stmt.getExpr() instanceof UnaryExpr);
   }
}
