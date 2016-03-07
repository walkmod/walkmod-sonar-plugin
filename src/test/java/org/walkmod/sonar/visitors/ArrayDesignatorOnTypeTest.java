package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.type.ReferenceType;

public class ArrayDesignatorOnTypeTest {

   @Test
   public void testSimpleCase() throws Exception {
      CompilationUnit cu = ASTManager.parse(
     "public class Foo { "+
        
         "public boolean test(int[] x[]) { "+
            "return false;"+
         " }"+
      " } ");
      ArrayDesignatorOnType visitor = new ArrayDesignatorOnType();
      visitor.visit(cu, null);
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      ReferenceType type = (ReferenceType) md.getParameters().get(0).getType();
      Assert.assertEquals(2, type.getArrayCount());
   }
}
