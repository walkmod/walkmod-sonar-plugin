package org.walkmod.sonar.visitors;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.test.SemanticTest;
import org.walkmod.refactor.visitors.MethodRefactor;
import org.walkmod.walkers.VisitorContext;

public class RemoveUnusedMethodParametersTest extends SemanticTest {

   private VisitorContext dummyContext() {
      Configuration conf = new ConfigurationImpl();
      ChainConfig cc = new ChainConfigImpl();
      cc.setWalkerConfig(new WalkerConfigImpl());
      conf.addChainConfig(cc);
      VisitorContext vc = new VisitorContext(cc);
      return vc;
   }

   @Test
   public void test() throws Exception {
      CompilationUnit cu = compile("public class Foo{ public void bar(String s) {} public void test() { bar(\"hello\"); }}");
      RemoveUnusedMethodParameters visitor = new RemoveUnusedMethodParameters();
      VisitorContext vc = dummyContext();
      cu.accept(visitor, vc);
      Map<String, String> rules = visitor.getRefactoringRules();
      Assert.assertNotNull(rules);
      Assert.assertEquals(1, rules.size());
      String value = rules.get("Foo:bar(java.lang.String s)");
      Assert.assertNotNull(value);
      Assert.assertEquals("Foo:bar()", value);
      
      MethodRefactor mr = new MethodRefactor();
      mr.setClassLoader(getClassLoader());
      mr.setRefactoringRules(rules);
      cu.accept(mr, vc);
      MethodDeclaration method = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(1);
      ExpressionStmt stmt= (ExpressionStmt) method.getBody().getStmts().get(0);
      MethodCallExpr mce = (MethodCallExpr) stmt.getExpression();
      Assert.assertTrue(mce.getArgs().isEmpty());
      
      System.out.println(cu.toString());
      
   }

}
