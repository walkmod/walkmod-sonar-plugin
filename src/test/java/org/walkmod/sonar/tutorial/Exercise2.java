package org.walkmod.sonar.tutorial;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;

public class Exercise2 extends SemanticTest{

   @Test
   public void test() throws Exception{
      CompilationUnit cu = compile("import java.util.List; public class Foo{}");
      Assert.assertNotNull(cu);
   }
}
