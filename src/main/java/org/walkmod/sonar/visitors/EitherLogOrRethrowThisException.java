package org.walkmod.sonar.visitors;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.javalang.ast.ConstructorSymbolData;
import org.walkmod.javalang.ast.MethodSymbolData;
import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.ObjectCreationExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.CatchClause;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.ThrowStmt;
import org.walkmod.javalang.ast.stmt.TypeDeclarationStmt;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class EitherLogOrRethrowThisException extends VoidVisitorAdapter<VisitorContext> {

   private List<String> validExceptions;

   public EitherLogOrRethrowThisException() {
      validExceptions = new LinkedList<String>();
      validExceptions.add(InterruptedException.class.getName());
      validExceptions.add(NumberFormatException.class.getName());
      validExceptions.add(ParseException.class.getName());
      validExceptions.add(MalformedURLException.class.getName());
   }

   private boolean isLoogingClass(String clazz) {
      return isCommonsLogging(clazz) || isSlf4j(clazz);
   }

   private boolean isCommonsLogging(String clazz) {
      return "org.apache.commons.logging.Log".equals(clazz);
   }

   private boolean isSlf4j(MethodCallExpr n) {
      MethodSymbolData msd = n.getSymbolData();
      if (msd != null) {
         Method md = msd.getMethod();
         String clazz = md.getDeclaringClass().getName();
         return isSlf4j(clazz);
      }
      return false;
   }

   private boolean isSlf4j(String clazz) {
      return "org.slf4j.Logger".equals(clazz);
   }

   private class BlockChecker extends VoidVisitorAdapter<VisitorContext> {
      private boolean isValid = true;

      @Override
      public void visit(ThrowStmt n, VisitorContext ctx) {
         if (n.getExpr() instanceof NameExpr) {
            isValid = false;
         } else {
            super.visit(n, ctx);
         }
      }

      @Override
      public void visit(MethodCallExpr n, VisitorContext ctx) {
         isValid = isValid && !isLogInfo(n, Exception.class);
         if (isValid) {
            super.visit(n, ctx);
         }
      }

      @Override
      public void visit(ObjectCreationExpr n, VisitorContext ctx) {
         isValid = isValid && !isRuntimeException(n, Exception.class);
         if (isValid) {
            super.visit(n, ctx);
         }
      }

      @Override
      public void visit(IfStmt n, VisitorContext ctx) {
         isValid = false;
      }

      @Override
      public void visit(TypeDeclarationStmt n, VisitorContext ctx) {

      }

      public boolean isValid() {
         return isValid;
      }

   }

   private boolean isLogInfo(MethodCallExpr n, Class<?> clazzArg) {
      if ("info".equals(n.getName())) {
         MethodSymbolData msd = n.getSymbolData();
         if (msd != null) {
            Method md = msd.getMethod();
            String clazz = md.getDeclaringClass().getName();
            if (isLoogingClass(clazz)) {
               List<Expression> args = n.getArgs();
               if (args != null && args.size() == 1) {
                  Expression arg = args.get(0);
                  SymbolData sd = arg.getSymbolData();
                  if (sd != null) {
                     if (clazzArg.isAssignableFrom(sd.getClazz())) {
                        return true;
                     }

                  }
               }
            }
         }
      }
      return false;
   }

   private boolean isRuntimeException(ObjectCreationExpr n, Class<?> clazzArg) {
      ConstructorSymbolData csd = n.getSymbolData();
      if (csd != null) {
         if (RuntimeException.class.getName().equals(csd.getName())) {
            List<Expression> args = n.getArgs();
            if (args != null && args.size() == 1) {
               Expression expr = args.get(0);
               SymbolData sd = expr.getSymbolData();
               if (sd != null) {
                  if (clazzArg.isAssignableFrom(sd.getClazz())) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private class LogCorrector extends VoidVisitorAdapter<VisitorContext> {

      private VariableDeclaratorId exceptionVar;

      public LogCorrector(VariableDeclaratorId exceptionVar) {
         this.exceptionVar = exceptionVar;
      }

      @Override
      public void visit(MethodCallExpr n, VisitorContext ctx) {

         super.visit(n, ctx);
         if (isLogInfo(n, String.class)) {
            if (isLogInfo(n, String.class)) {
               List<Expression> newArgs = new LinkedList<Expression>();

               newArgs.add(n.getArgs().get(0));

               newArgs.add(new NameExpr(exceptionVar.getName()));

               MethodCallExpr mce = new MethodCallExpr(n.getScope(), "info", newArgs);
               n.getParentNode().replaceChildNode(n, mce);
            }
         }
      }

      @Override
      public void visit(ObjectCreationExpr n, VisitorContext ctx) {
         super.visit(n, ctx);
         if (isRuntimeException(n, String.class)) {
            List<Expression> newArgs = new LinkedList<Expression>();
            newArgs.add(new NameExpr(exceptionVar.getName()));
            ObjectCreationExpr oce = new ObjectCreationExpr(null, n.getType(), newArgs);
            n.getParentNode().replaceChildNode(n, oce);
         }
      }

   }

   @Override
   public void visit(CatchClause n, VisitorContext ctx) {
      super.visit(n, ctx);
      List<Type> types = n.getExcept().getTypes();
      boolean isInvalid = true;
      if (types != null) {
         Iterator<Type> it = types.iterator();

         while (it.hasNext() && isInvalid) {
            Type type = it.next();
            SymbolData sd = type.getSymbolData();
            isInvalid = !validExceptions.contains(sd.getClazz().getName());
         }
      }
      if (isInvalid) {
         BlockStmt blockStmts = n.getCatchBlock();
         if (blockStmts != null) {
            BlockChecker checker = new BlockChecker();
            blockStmts.accept(checker, ctx);
            if (checker.isValid()) {
               LogCorrector visitor = new LogCorrector(n.getExcept().getId());
               blockStmts.accept(visitor, ctx);
            }
         }
      }

   }

}
