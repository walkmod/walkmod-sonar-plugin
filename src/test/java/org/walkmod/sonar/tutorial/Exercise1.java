package org.walkmod.sonar.tutorial;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.CatchClause;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

public class Exercise1 {

   @Test
   public void testParsing() throws Exception{
      CompilationUnit cu = ASTManager.parse("public class Foo{ "+
            "public void bar(InputStream is) {"+
            " try{ "+
               "is.read(); "+
            "}catch(Exception e) {"+"}"
         + " }"+
      " }");
      Assert.assertNotNull(cu);
      HelloVisitor visitor = new HelloVisitor();
      cu.accept(visitor, null);
      
      ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration)cu.getTypes().get(0);
      Assert.assertEquals("Hello", cid.getName());
      System.out.println(cu.toString());
   }
   
   public class HelloVisitor extends VoidVisitorAdapter<VisitorContext>{
      
      @Override
      public void visit(ClassOrInterfaceDeclaration n, VisitorContext ctx){
         n.setName("Hello");
         super.visit(n, ctx);
      }
      
      @Override
      public void visit(MethodDeclaration n, VisitorContext ctx){
         System.out.println(n.getName());
         super.visit(n, ctx);
      }
      
      @Override
      public void visit(CatchClause n, VisitorContext ctx){
         BlockStmt block = n.getCatchBlock();
         List<Statement> stmts = block.getStmts();
         if(stmts == null || stmts.isEmpty()){
            if(stmts == null){
               stmts = new LinkedList<Statement>();
               block.setStmts(stmts);
            }
            ExpressionStmt stmt = new ExpressionStmt(new MethodCallExpr(new NameExpr("n"), "printStacktrace"));
            stmts.add(stmt);
            
         }
         super.visit(n, ctx);
      }
      
   }
}
