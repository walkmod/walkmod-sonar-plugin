/*
 * SYSTEMi Copyright © 2015, MetricStream, Inc. All rights reserved.
 * 
 * Walkmod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Walkmod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mohanasundar N(mohanasundar.n@metricstream.com)
 * created 05/01/2015
 */

package org.walkmod.sonar.visitors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.DoStmt;
import org.walkmod.javalang.ast.stmt.ForStmt;
import org.walkmod.javalang.ast.stmt.ForeachStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.ast.stmt.SwitchEntryStmt;
import org.walkmod.javalang.ast.stmt.WhileStmt;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class AddCurlyBrackets.
 *
 * @author mohanasundar.n
 */
public class AddCurlyBrackets extends VoidVisitorAdapter<VisitorContext> {

   /** The disable list. */
   private final List<String> disableList;

   /**
    * The Enum Block.
    */
   private enum Block {

      /** The if. */
      IF("IF"),

      /** The else. */
      ELSE("ELSE"),

      /** The for. */
      FOR("FOR"),

      /** The do. */
      DO("DO"),

      /** The while. */
      WHILE("WHILE"),

      /** The switch. */
      SWITCH("SWITCH"),

      /** The foreach. */
      FOREACH("FOREACH");

      /** The name. */
      String name;

      /**
       * Instantiates a new block.
       *
       * @param name
       *           the name
       */
      private Block(String name) {
         this.name = name;
      }
   }

   /**
    * Instantiates a new adds the curly brackets.
    */
   public AddCurlyBrackets() {
      disableList = new ArrayList<String>();
   }

   /**
    * Sets the disable for.
    *
    * @param disableFor
    *           the new disable for
    */
   public void setDisableFor(String disableFor) {
      if (disableFor != null) {
         StringTokenizer tokenizer = new StringTokenizer(disableFor, ",");
         while (tokenizer.hasMoreTokens()) {
            String string = tokenizer.nextToken().trim();
            disableList.add(string.toUpperCase());
         }
      }
   }

   /**
    * Checks if is enabled.
    *
    * @param block
    *           the block
    * @return true, if is enabled
    */
   private boolean isEnabled(Block block) {
      return !disableList.contains(block.name);
   }

   /**
    * Checks if is block stmt.
    *
    * @param thenStmt
    *           the then stmt
    * @return true, if is block stmt
    */
   private boolean isBlockStmt(Statement thenStmt) {
      if (thenStmt instanceof BlockStmt) {
         return true;
      }
      return false;
   }

   private BlockStmt createBlockStmt(Statement stmt) {

      List<Statement> statements = new ArrayList<Statement>();
      BlockStmt block = new BlockStmt();
      try {
         statements.add(stmt.clone());

         block.setStmts(statements);
      } catch (CloneNotSupportedException e) {
      }
      return block;
   }
   
   private BlockStmt createBlockStmt(List<Statement> stmts) {

      List<Statement> statements = new ArrayList<Statement>();
      BlockStmt block = new BlockStmt();
      try {
         for(Statement stmt: stmts){
            statements.add(stmt.clone());
         }

         block.setStmts(statements);
      } catch (CloneNotSupportedException e) {
      }
      return block;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.IfStmt,
    * java.lang.Object)
    */
   @Override
   public void visit(IfStmt n, VisitorContext arg) {
      if (isEnabled(Block.IF)) {
         Statement thenStmt = n.getThenStmt();
         if (!isBlockStmt(thenStmt)) {
            n.setThenStmt(createBlockStmt(thenStmt));
         }

      }
      if (isEnabled(Block.ELSE)) {
         Statement elseStmt = n.getElseStmt();
         if (elseStmt != null && !(elseStmt instanceof IfStmt)) {
            if (!isBlockStmt(elseStmt)) {
               n.setElseStmt(createBlockStmt(elseStmt));
            }
         }
      }
      super.visit(n, arg);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.ForStmt,
    * java.lang.Object)
    */
   @Override
   public void visit(ForStmt n, VisitorContext arg) {
      if (isEnabled(Block.FOR)) {
         Statement bodyStmt = n.getBody();
         if (!isBlockStmt(bodyStmt)) {
            n.setBody(createBlockStmt(bodyStmt));
         }
      }
      super.visit(n, arg);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.
    * WhileStmt, java.lang.Object)
    */
   @Override
   public void visit(WhileStmt n, VisitorContext arg) {
      if (isEnabled(Block.WHILE)) {
         Statement bodyStmt = n.getBody();
         if (!isBlockStmt(bodyStmt)) {           
            n.setBody(createBlockStmt(bodyStmt));
         }
      }
      super.visit(n, arg);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.DoStmt,
    * java.lang.Object)
    */
   @Override
   public void visit(DoStmt n, VisitorContext arg) {
      if (isEnabled(Block.DO)) {
         Statement bodyStmt = n.getBody();
         if (!isBlockStmt(bodyStmt)) {            
            n.setBody(createBlockStmt(bodyStmt));
         }
      }
      super.visit(n, arg);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.
    * ForeachStmt, java.lang.Object)
    */
   @Override
   public void visit(ForeachStmt n, VisitorContext arg) {
      if (isEnabled(Block.FOREACH)) {
         Statement bodyStmt = n.getBody();
         if (!isBlockStmt(bodyStmt)) {           
            n.setBody(createBlockStmt(bodyStmt));
         }
      }
      super.visit(n, arg);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod.javalang.ast.stmt.
    * SwitchEntryStmt, java.lang.Object)
    */
   @Override
   public void visit(SwitchEntryStmt n, VisitorContext arg) {
      if (isEnabled(Block.SWITCH)) {
         List<Statement> statements = n.getStmts();
         if (statements != null) {
            if (statements.size() > 0 && !isBlockStmt(statements.get(0))) {
               List<Statement> newStatements = new LinkedList<Statement>();
               newStatements.add(createBlockStmt(statements));
               n.setStmts(newStatements);
            }
         }
      }
      super.visit(n, arg);
   }
}
