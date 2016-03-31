package org.walkmod.sonar.visitors;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.ast.type.ReferenceType;
import org.walkmod.javalang.test.SemanticTest;


public class DeclarationsShouldUseCollectionInterfacesTest extends SemanticTest{

   @Test
   public void testSimpleCase() throws Exception {
      CompilationUnit cu = compile("import java.util.LinkedList; "+
     "public class Foo { "+
         "private LinkedList list = new LinkedList(); "+
         "public boolean test() { "+
            "return list.isEmpty();"+
         " }"+
      " } ");
      DeclarationsShouldUseCollectionInterfaces visitor = new DeclarationsShouldUseCollectionInterfaces();
      visitor.visit(cu, null);
      FieldDeclaration fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(0);
      ClassOrInterfaceType type = (ClassOrInterfaceType) fd.getType();
      Assert.assertEquals("List", type.getName());
   }
   

   @Test
   public void testNoChanges() throws Exception {
      CompilationUnit cu = compile("import java.util.LinkedList; "+
     "public class Foo { "+
         "private LinkedList list = new LinkedList(); "+
         "public LinkedList test() { "+
            "return list;"+
         " }"+
      " } ");
      DeclarationsShouldUseCollectionInterfaces visitor = new DeclarationsShouldUseCollectionInterfaces();
      visitor.visit(cu, null);
      FieldDeclaration fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(0);
      ReferenceType type = (ReferenceType) fd.getType();
      Assert.assertEquals("LinkedList", ((ClassOrInterfaceType)type.getType()).getName());
   }
   
   
   @Test
   public void testAssignments() throws Exception {
      CompilationUnit cu = compile("import java.util.LinkedList; "+
     "public class Foo { "+
         "private LinkedList list = new LinkedList(); "+
         "private LinkedList listAux = new LinkedList(); "+
         "public void test() { "+
            "list = listAux;"+
         " }"+
      " } ");
      DeclarationsShouldUseCollectionInterfaces visitor = new DeclarationsShouldUseCollectionInterfaces();
      visitor.visit(cu, null);
      FieldDeclaration fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(0);
      ClassOrInterfaceType type = (ClassOrInterfaceType) fd.getType();
      Assert.assertEquals("List", type.getName());
      
      fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(1);
      type = (ClassOrInterfaceType) fd.getType();
      Assert.assertEquals("List", type.getName());
   }
   

   @Test
   public void testSimpleCaseWithVars() throws Exception {
      CompilationUnit cu = compile("import java.util.LinkedList; "+
     "public class Foo { "+
         "public void test() { "+
            "LinkedList list = new LinkedList();"+
         " }"+
      " } ");
      DeclarationsShouldUseCollectionInterfaces visitor = new DeclarationsShouldUseCollectionInterfaces();
      visitor.visit(cu, null);
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      List<Statement> stmts = md.getBody().getStmts();
      ExpressionStmt eStmt = (ExpressionStmt) stmts.get(0);
      VariableDeclarationExpr vde = (VariableDeclarationExpr)eStmt.getExpression();
      Assert.assertEquals("List", vde.getType().toString());
   }
   
   
   @Test
   public void testTypeArgs() throws Exception {
      CompilationUnit cu = compile(
     "import java.util.ArrayList; public class Foo { ArrayList<String> inTitles; "+
        
         "public void test() { "+
            "ArrayList<String> titles = (ArrayList<String>) inTitles.clone();"+
         " }"+
      " } ");
      DeclarationsShouldUseCollectionInterfaces visitor = new DeclarationsShouldUseCollectionInterfaces();
      visitor.visit(cu, null);
      
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(1);
      ExpressionStmt stmt = (ExpressionStmt) md.getBody().getStmts().get(0);
      VariableDeclarationExpr expr = (VariableDeclarationExpr) stmt.getExpression();
      ClassOrInterfaceType type = (ClassOrInterfaceType)expr.getType();
      
      Assert.assertEquals(1, type.getTypeArgs().size());
   }
   
}
