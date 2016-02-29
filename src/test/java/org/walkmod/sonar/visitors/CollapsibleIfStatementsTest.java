package org.walkmod.sonar.visitors;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;

public class CollapsibleIfStatementsTest {

   @Test
   public void test() throws Exception {
      CompilationUnit cu = ASTManager.parse(new File("src/test/resources/examples/collapsibleIfStatements.txt"));
      CollapsibleIfStatements visitor = new CollapsibleIfStatements();
      visitor.visit(cu, null);
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      BlockStmt block = md.getBody();
      IfStmt ifStmt = (IfStmt) block.getStmts().get(0);
      BlockStmt stmt = (BlockStmt) ifStmt.getThenStmt();
      Assert.assertNull(stmt.getStmts());
      Assert.assertTrue(ifStmt.getCondition() instanceof BinaryExpr);
   }
}
