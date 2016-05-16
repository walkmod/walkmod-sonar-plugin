package org.walkmod.sonar.visitors;

import java.util.LinkedList;
import java.util.List;

import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.TypeDeclaration;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.expr.AnnotationExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;

public class RefactoringUtils {

   
   public static void addFieldDeclaration(Node n, FieldDeclaration field){
      Node parent = n.getParentNode();
      while (parent != null && !(parent instanceof TypeDeclaration)
            && !(parent instanceof CompilationUnit)) {
         parent = parent.getParentNode();
      }
      if (parent instanceof TypeDeclaration) {
         TypeDeclaration td = (TypeDeclaration) parent;
         List<BodyDeclaration> members = new LinkedList<BodyDeclaration>(td.getMembers());

         members.add(0, field);
         td.setMembers(members);
      }
   }
   
   
   public static void addLOG(Node n, String logVar, String logType, List<AnnotationExpr> annotations){
      Node parent = n.getParentNode();
      while (parent != null && !(parent instanceof TypeDeclaration)
            && !(parent instanceof CompilationUnit)) {
         parent = parent.getParentNode();
      }
      if (parent instanceof TypeDeclaration) {
         TypeDeclaration td = (TypeDeclaration) parent;
         List<BodyDeclaration> members = new LinkedList<BodyDeclaration>(td.getMembers());

         List<VariableDeclarator> vars = new LinkedList<VariableDeclarator>();
         vars.add(new VariableDeclarator(new VariableDeclaratorId(logVar), getInit(logType, td)));

         FieldDeclaration fd = new FieldDeclaration(null, ModifierSet.PRIVATE, annotations,
               new ClassOrInterfaceType(logType), vars);
         members.add(0, fd);
         td.setMembers(members);
      }
   }
   
   private static Expression getInit(String logType, TypeDeclaration td) {
      if ("java.util.logging.Logger".equals(logType) ) {
         List<Expression> args = new LinkedList<Expression>();
         args.add(new NameExpr(td.getName()));
         return  new MethodCallExpr(new NameExpr(logType), "getLogger", args);
      }
      return null;
   }
}
