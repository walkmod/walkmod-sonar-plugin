package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.ReturnStmt;
import org.walkmod.javalang.test.SemanticTest;

public class UseCollectionIsEmptyTest extends SemanticTest {

   @Test
   public void testEqualsToZero() throws Exception {
      CompilationUnit cu = compile(
            "import java.util.List; public class Foo { public boolean testIsEmpty(List list){ return list.size() == 0; }}");

      UseCollectionIsEmpty visitor = new UseCollectionIsEmpty();
      cu.accept(visitor, null);

      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      
      BlockStmt block = md.getBody();
      ReturnStmt returnStmt = (ReturnStmt) block.getStmts().get(0);
      
      Assert.assertTrue(returnStmt.getExpr() instanceof MethodCallExpr);
      
      MethodCallExpr mce = (MethodCallExpr) returnStmt.getExpr();
      
      Assert.assertEquals("isEmpty", mce.getName());
   }
   
   @Test
   public void testEqualsToZeroWithOtherBinOp() throws Exception {
      CompilationUnit cu = compile(
            "import java.util.List; public class Foo { public boolean testIsEmpty(List list){ return (list.size() == 0 || true); }}");

      UseCollectionIsEmpty visitor = new UseCollectionIsEmpty();
      cu.accept(visitor, null);

      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      
      BlockStmt block = md.getBody();
      ReturnStmt returnStmt = (ReturnStmt) block.getStmts().get(0);
      
      EnclosedExpr enclosedExpr = (EnclosedExpr)returnStmt.getExpr();
      BinaryExpr be = (BinaryExpr)enclosedExpr.getInner();
      MethodCallExpr mce = (MethodCallExpr) be.getLeft();
      Assert.assertEquals("isEmpty", mce.getName());
   }
}
