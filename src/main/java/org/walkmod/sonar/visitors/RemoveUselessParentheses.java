package org.walkmod.sonar.visitors;

import java.util.HashMap;
import java.util.Map;

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.CastExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

public class RemoveUselessParentheses extends VoidVisitorAdapter<VisitorContext> {

   //https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html
   private Map<BinaryExpr.Operator, Integer> precedence = new HashMap<BinaryExpr.Operator, Integer>();

   public RemoveUselessParentheses() {
      precedence.put(BinaryExpr.Operator.and, 10);
      precedence.put(BinaryExpr.Operator.or, 10);
      precedence.put(BinaryExpr.Operator.binAnd, 9);
      precedence.put(BinaryExpr.Operator.binOr, 9);
      precedence.put(BinaryExpr.Operator.xor, 9);
      precedence.put(BinaryExpr.Operator.equals, 8);
      precedence.put(BinaryExpr.Operator.notEquals, 8);
      precedence.put(BinaryExpr.Operator.less, 7);
      precedence.put(BinaryExpr.Operator.lessEquals, 7);
      precedence.put(BinaryExpr.Operator.greater, 7);
      precedence.put(BinaryExpr.Operator.greaterEquals, 7);
      precedence.put(BinaryExpr.Operator.lShift, 6);
      precedence.put(BinaryExpr.Operator.rSignedShift, 6);
      precedence.put(BinaryExpr.Operator.rUnsignedShift, 6);
      precedence.put(BinaryExpr.Operator.plus, 5);
      precedence.put(BinaryExpr.Operator.minus, 5);
      precedence.put(BinaryExpr.Operator.times, 4);
      precedence.put(BinaryExpr.Operator.divide, 4);
      precedence.put(BinaryExpr.Operator.remainder, 4);
   }

   @Override
   public void visit(EnclosedExpr n, VisitorContext ctx) {
      Expression inner = n.getInner();
      if (!(inner instanceof BinaryExpr)) {
         if (!(inner instanceof CastExpr)) {
            n.getParentNode().replaceChildNode(n, inner);
            inner.accept(this, ctx);
         }
      } else {
         BinaryExpr be = (BinaryExpr) inner;
         if (!(n.getParentNode() instanceof UnaryExpr)) {
            if (be.getLeft() instanceof EnclosedExpr) {
               n.getParentNode().replaceChildNode(n, inner);
               inner.accept(this, ctx);
            } else if (be.getRight() instanceof EnclosedExpr) {
               n.getParentNode().replaceChildNode(n, inner);
               inner.accept(this, ctx);
            } else {
               Node parent = n.getParentNode();
               if (parent instanceof BinaryExpr) {
                  BinaryExpr parentBinary = (BinaryExpr) parent;
                  if (precedence.get(parentBinary.getOperator()) > precedence.get(be.getOperator())) {
                     n.getParentNode().replaceChildNode(n, inner);
                     inner.accept(this, ctx);
                  } else {
                     super.visit(n, ctx);
                  }
               } else {
                  super.visit(n, ctx);
               }
            }
         } else {
            inner.accept(this, ctx);
         }
      }
   }
}
