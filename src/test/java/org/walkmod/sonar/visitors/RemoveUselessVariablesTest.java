package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.test.SemanticTest;

public class RemoveUselessVariablesTest extends SemanticTest{
   
   @Test
   public void test() throws Exception{
      CompilationUnit cu = ASTManager.parse("public class Foo{ public void get() { int x; } }");
      RemoveUselessVariables visitor = new RemoveUselessVariables();
      cu.accept(visitor, null);
      MethodDeclaration md = (MethodDeclaration)cu.getTypes().get(0).getMembers().get(0);
      Assert.assertTrue(md.getBody().getStmts().isEmpty());
   }
   
   @Test
   public void testExtra() throws Exception{
      CompilationUnit cu = compile("public class Foo{ private String name_;String q1 = \"a\";         String q2;         String q4;         String q5; "
            + " public void Create_GUI(String para) {      System.out.println(\"create GUI\");      "
            + "try {         "
            + "String groupcode1;         "
            + "double groupcode2;         "
            + "int groupcode3;   q2 = \"\";      "
            + "float groupcode4;         "
            + "String Froupcode5 = \"\";   String  AmtFormat = \"\";     "
            + "Froupcode5 = \"\";         "
            + "AmtFormat = \"##,##,##0.00\";               "
            + "if ( (Froupcode5.length() > 0)) {            "
            + "   AmtFormat = \"aa\";      "
            + "} } catch (Exception e) {                   }    }}");
      RemoveUselessVariables visitor = new RemoveUselessVariables();
      cu.accept(visitor, null);
      System.out.println(cu.toString());
   }
}
