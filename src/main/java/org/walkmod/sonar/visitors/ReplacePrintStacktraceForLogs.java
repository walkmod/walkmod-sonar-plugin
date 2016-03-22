package org.walkmod.sonar.visitors;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.SymbolDefinition;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.TypeDeclaration;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.expr.AnnotationExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MarkerAnnotationExpr;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

import com.alibaba.fastjson.JSONArray;

@RequiresSemanticAnalysis
public class ReplacePrintStacktraceForLogs extends VoidVisitorAdapter<VisitorContext> {

   private String logVar = "log";

   private String logType = "java.util.logging.Logger";

   private List<AnnotationExpr> annotations = new LinkedList<AnnotationExpr>();

   private Expression init = null;
   
   private String logMethod = "severe";
   
   private boolean attachTheException = false;
   
   public void setLogVar(String logVar){
      this.logVar = logVar;
   }
   
   public void setLogType(String logType){
      this.logType = logType;
   }
   
   public void setInitExpression(String initExpression) throws ParseException {
      if (initExpression != null) {
         init = (Expression) ASTManager.parse(Expression.class, initExpression);
      }
   }
   
   public void setLogMethod(String logMethod){
      this.logMethod = logMethod;
   }
   
   public void setAttachTheException(boolean attachTheException){
      this.attachTheException = attachTheException;
   }

   private Expression getInit(TypeDeclaration td) {
      if ("java.util.logging.Logger".equals(logType) && init == null) {
         List<Expression> args = new LinkedList<Expression>();
         args.add(new NameExpr(td.getName()));
         init = new MethodCallExpr(new NameExpr(logType), "getLogger", args);
      }
      return init;
   }

   public void setAnnotationExpressions(JSONArray annotations) {
      if (annotations != null) {
         Iterator<Object> it = annotations.iterator();
         while (it.hasNext()) {
            String annotation = it.next().toString();
            AnnotationExpr ann = new MarkerAnnotationExpr(new NameExpr(annotation));
            this.annotations.add(ann);
         }
      }
   }

   @Override
   public void visit(MethodCallExpr n, VisitorContext ctx) {

      if ("printStackTrace".equals(n.getName())) {
         Expression scope = n.getScope();
         if (scope != null && scope instanceof NameExpr) {
            SymbolData sd = scope.getSymbolData();
            NameExpr var = (NameExpr) scope;
            if (sd != null) {
               if (Throwable.class.isAssignableFrom(sd.getClazz())) {
                  
                  List<Expression> args = new LinkedList<Expression>();
                  args.add(new MethodCallExpr(new NameExpr(var.getName()), "getMessage"));
                  if(attachTheException){
                     args.add(new NameExpr(var.getName()));
                  }
                  n.getParentNode().replaceChildNode(n, new MethodCallExpr(new NameExpr(logVar), logMethod, args));

                  Map<String, SymbolDefinition> defs = n.getVariableDefinitions();
                  if (!defs.containsKey(logVar)) {
                     Node parent = n.getParentNode();
                     while (parent != null && !(parent instanceof TypeDeclaration)
                           && !(parent instanceof CompilationUnit)) {
                        parent = parent.getParentNode();
                     }
                     if (parent instanceof TypeDeclaration) {
                        TypeDeclaration td = (TypeDeclaration) parent;
                        List<BodyDeclaration> members = new LinkedList<BodyDeclaration>(td.getMembers());

                        List<VariableDeclarator> vars = new LinkedList<VariableDeclarator>();
                        vars.add(new VariableDeclarator(new VariableDeclaratorId(logVar), getInit(td)));

                        FieldDeclaration fd = new FieldDeclaration(null, ModifierSet.PRIVATE, annotations,
                              new ClassOrInterfaceType(logType), vars);
                        members.add(0, fd);
                        td.setMembers(members);
                     }
                  }
               }
            }
         }
      }
      super.visit(n, ctx);
   }

}
