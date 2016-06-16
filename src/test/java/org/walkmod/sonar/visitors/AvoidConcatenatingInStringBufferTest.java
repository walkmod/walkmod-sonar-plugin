package org.walkmod.sonar.visitors;

import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;

public class AvoidConcatenatingInStringBufferTest extends SemanticTest{

   @Test
   public void test2() throws Exception{
      String code =
            
            "public class Foo{ public void bar(){"+
            "StringBuffer sb = new StringBuffer();"+
             "sb.append(\"hello\"+\"r\");"+
            "}}";
      CompilationUnit cu = compile(code);
      AvoidConcatenatingInStringBuffer visitor = new AvoidConcatenatingInStringBuffer();
      visitor.visit(cu, null);
      System.out.println(cu.toString());
   }
}
