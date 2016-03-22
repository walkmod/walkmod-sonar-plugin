package org.walkmod.sonar.visitors;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;

import com.alibaba.fastjson.JSONArray;

public class ReplacePrintStacktraceForLogsTest extends SemanticTest {

   @Test
   public void test() throws Exception {
      CompilationUnit cu = compile("import java.io.File; public class Foo{" + " public void bar(File f) { " + "try{ "
            + " f.createNewFile();" + " }catch(Exception e){ e.printStackTrace(); } }}");

      ReplacePrintStacktraceForLogs visitor = new ReplacePrintStacktraceForLogs();
      cu.accept(visitor, null);

      System.out.println(cu.toString());

   }

   @Test
   public void testCustom() throws Exception {
      CompilationUnit cu = compile("import java.io.File; public class Foo{" + " public void bar(File f) { " + "try{ "
            + " f.createNewFile();" + " }catch(Exception e){ e.printStackTrace(); } }}");

      ReplacePrintStacktraceForLogs visitor = new ReplacePrintStacktraceForLogs();
      visitor.setLogType("com.svm.lrp.nfr.jsf.utils.ILogger");
      visitor.setAttachTheException(true);
      visitor.setLogMethod("fatal");
      List<Object> annotations = new LinkedList<Object>();
      annotations.add("javax.inject.Inject");
      JSONArray array = new JSONArray(annotations);
      visitor.setAnnotationExpressions(array);
      cu.accept(visitor, null);

      System.out.println(cu.toString());
   }
}
