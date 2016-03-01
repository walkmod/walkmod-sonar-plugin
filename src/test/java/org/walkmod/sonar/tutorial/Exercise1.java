package org.walkmod.sonar.tutorial;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;

public class Exercise1 {

   @Test
   public void testParsing() throws Exception{
      CompilationUnit cu = ASTManager.parse("public class Foo{}");
      Assert.assertNotNull(cu);
   }
}
