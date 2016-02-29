package org.walkmod.sonar.visitors;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.FieldDeclaration;
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
}
