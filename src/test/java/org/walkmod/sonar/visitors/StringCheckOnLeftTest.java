package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.ReturnStmt;

public class StringCheckOnLeftTest {

   @Test
   public void test() throws Exception {
      CompilationUnit cu = ASTManager.parse("public class Foo { public boolean bar(String x) { return x.equals(\"hello\"); }}");
      StringCheckOnLeft visitor = new StringCheckOnLeft();
      visitor.visit(cu, null);
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      BlockStmt block = md.getBody();
      ReturnStmt returnStmt = (ReturnStmt) block.getStmts().get(0);
      MethodCallExpr mce = (MethodCallExpr)returnStmt.getExpr();
      Assert.assertTrue(mce.getScope() instanceof StringLiteralExpr);
      Assert.assertTrue(mce.getArgs().get(0) instanceof NameExpr);
   }
}
