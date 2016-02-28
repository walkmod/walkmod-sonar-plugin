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
import java.util.List;

import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.BreakStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.ast.stmt.SwitchEntryStmt;
import org.walkmod.javalang.ast.stmt.SwitchStmt;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

/**
 * The Class AddSwitchDefaultCase.
 * 
 * @author mohanasundar.n
 *
 */
public class AddSwitchDefaultCase extends VoidVisitorAdapter<VisitorContext> {

	/** The curly bracket. */
	private boolean curlyBracket = true;

	/**
	 * Checks if is curly bracket.
	 *
	 * @return true, if is curly bracket
	 */
	public boolean isCurlyBracket() {
		return curlyBracket;
	}

	/**
	 * Sets the curly bracket.
	 *
	 * @param curlyBracket
	 *            the new curly bracket
	 */
	public void setCurlyBracket(boolean curlyBracket) {
		this.curlyBracket = curlyBracket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.walkmod.javalang.visitors.VoidVisitorAdapter#visit(org.walkmod. javalang.ast.stmt.SwitchStmt,
	 * java.lang.Object)
	 */
	@Override
	public void visit(SwitchStmt n, VisitorContext arg) {
		List<SwitchEntryStmt> entries = n.getEntries();
		boolean isDefault = false;
		for (SwitchEntryStmt switchEntryStmt : entries) {
			if (switchEntryStmt.getLabel() == null) {
				isDefault = true;
			}
		}
		if (!isDefault) {
			List<Statement> bstmts = new ArrayList<Statement>();
			BlockStmt blockStmt = new BlockStmt();

			List<Statement> stmts = new ArrayList<Statement>();
			BreakStmt breakStmt = new BreakStmt();
			stmts.add(breakStmt);

			blockStmt.setStmts(stmts);
			bstmts.add(blockStmt);

			SwitchEntryStmt entryStmt = new SwitchEntryStmt();
			if (isCurlyBracket()) {
				entryStmt.setStmts(bstmts);
			} else {
				entryStmt.setStmts(stmts);
			}
			entries.add(entryStmt);
		}
		super.visit(n, arg);
	}
}
