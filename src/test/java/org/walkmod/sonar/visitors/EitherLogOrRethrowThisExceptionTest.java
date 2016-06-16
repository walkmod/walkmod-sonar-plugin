package org.walkmod.sonar.visitors;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.stmt.CatchClause;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.ast.stmt.TryStmt;
import org.walkmod.javalang.test.SemanticTest;

public class EitherLogOrRethrowThisExceptionTest extends SemanticTest{

   @Test
   public void testLOGInfoCase() throws Exception {
      CompilationUnit cu = compile(
            "import org.apache.commons.logging.Log;"+
            "import org.apache.commons.logging.LogFactory;"+
            "public class Foo { "+
                "private static final Log LOG = LogFactory.getLog(Foo.class);"+
            
                "public void test() throws Exception{ "+
                   "try{"+
                   "test();"+
                   "}catch(Exception e){"+
                   "LOG.info(\"error\");"+
                   "}"+
                " }"+
             " } ");
      
      EitherLogOrRethrowThisException visitor = new EitherLogOrRethrowThisException();
      cu.accept(visitor, null);
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(1);
      List<Statement> stmts = md.getBody().getStmts();
      if(stmts != null){
         TryStmt tryStmt = (TryStmt) stmts.get(0);
         CatchClause clause = tryStmt.getCatchs().get(0);
         Assert.assertEquals("LOG.info(\"error\", e);", clause.getCatchBlock().getStmts().get(0).toString());
      }
   }
   
   @Test
   public void testException() throws Exception{
      //throw new RuntimeException("context");
      
      CompilationUnit cu = compile(
           
            "public class Foo { "+
             
                "public void test() throws Exception{ "+
                   "try{"+
                   "test();"+
                   "}catch(Exception e){"+
                   "new RuntimeException(\"context\");"+
                   "}"+
                " }"+
             " } ");
      
      EitherLogOrRethrowThisException visitor = new EitherLogOrRethrowThisException();
      cu.accept(visitor, null);
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      List<Statement> stmts = md.getBody().getStmts();
      if(stmts != null){
         TryStmt tryStmt = (TryStmt) stmts.get(0);
         CatchClause clause = tryStmt.getCatchs().get(0);
         Assert.assertEquals("new RuntimeException(e);", clause.getCatchBlock().getStmts().get(0).toString());
      }
   }
   
   
   public void testMultipleArgsInSlf4j(){}
   
   
   public void testChangeWarnMessagesToInfo(){}
   
   
   public void testAddLoggerWhenNeeded(){}
  
   
   
}
