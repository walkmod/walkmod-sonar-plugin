package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;


public class RemoveEmptyMethodTest extends SemanticTest {

   @Test
   public void testRemoval() throws Exception{
      CompilationUnit cu = compile("public class Foo{ private void bar() {} }");
      RemoveEmptyMethod visitor = new RemoveEmptyMethod();
      cu.accept(visitor, null);
      Assert.assertTrue(cu.getTypes().get(0).getMembers().isEmpty());
   }
   
   @Test
   public void testIsNotRemoved() throws Exception{
      CompilationUnit cu = compile("public class Foo{ private void bar() {} private void test() {bar();} }");
      RemoveEmptyMethod visitor = new RemoveEmptyMethod();
      cu.accept(visitor, null);
      Assert.assertEquals(2, cu.getTypes().get(0).getMembers().size());
   }
}
