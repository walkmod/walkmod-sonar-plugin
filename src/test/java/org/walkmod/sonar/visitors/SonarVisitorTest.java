package org.walkmod.sonar.visitors;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;
import org.walkmod.walkers.VisitorContext;

public class SonarVisitorTest extends SemanticTest{

   
    
    @Test
    public void testRemoval() throws Exception{
       CompilationUnit cu = compile("import java.util.List; public class Foo{}");
      
       SonarVisitor visitor = new SonarVisitor(){
           public List<String> getRuleSet(){
               List<String> result = new LinkedList<String>();
               result.add("UselessImportCheck");
               return result;
           }
       };
     
       cu.accept(visitor, new VisitorContext());
       Assert.assertTrue(cu.getImports().isEmpty());
    }
    
}
