package org.walkmod.sonar.visitors;

import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;

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
}
