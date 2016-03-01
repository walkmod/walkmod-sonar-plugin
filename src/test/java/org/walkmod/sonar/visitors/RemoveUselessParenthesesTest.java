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
      System.out.println(cu.toString());
      
   }
}
