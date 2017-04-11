/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.visitors;

import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.ConditionalExpressionTree;
import org.sonar.plugins.java.api.tree.DoWhileStatementTree;
import org.sonar.plugins.java.api.tree.ForEachStatement;
import org.sonar.plugins.java.api.tree.ForStatementTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ReturnStatementTree;
import org.sonar.plugins.java.api.tree.ThrowStatementTree;
import org.sonar.plugins.java.api.tree.WhileStatementTree;

//FIXME compute also conditional OR and conditonal AND
/**
 * TODO
 * @author Tomas Lestyan
 */
public class ComplexityVisitor extends AVisitor {

	public static final String KEY = "cyclo";
	private int complexity = 0;

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getID()
	 */
	@Override
	public String getKey() {
		return KEY;
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getScope()
	 */
	@Override
	public Scope getScope() {
		return Scope.METHOD;
	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#getResult()
	 */
	@Override
	public int getResult() {
		return complexity;	}

	/* (non-Javadoc)
	 * @see main.java.visitors.ADisharmonyVisitor#scanMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void scanMethod(MethodTree tree) {
		complexity = 0;
		scan(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitCaseLabel(org.sonar.plugins.java.api.tree.CaseLabelTree)
	 */
	@Override
	public void visitCaseLabel(CaseLabelTree tree) {
	     if (!"default".equals(tree.caseOrDefaultKeyword().text())) {
	    	 complexity++;
	        }
	     super.visitCaseLabel(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitIfStatement(org.sonar.plugins.java.api.tree.IfStatementTree)
	 */
	@Override
	public void visitIfStatement(IfStatementTree tree) {
		complexity++;
		super.visitIfStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitForStatement(org.sonar.plugins.java.api.tree.ForStatementTree)
	 */
	@Override
	public void visitForStatement(ForStatementTree tree) {
		complexity++;
		super.visitForStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitForEachStatement(org.sonar.plugins.java.api.tree.ForEachStatement)
	 */
	@Override
	public void visitForEachStatement(ForEachStatement tree) {
		complexity++;
		super.visitForEachStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitDoWhileStatement(org.sonar.plugins.java.api.tree.DoWhileStatementTree)
	 */
	@Override
	public void visitDoWhileStatement(DoWhileStatementTree tree) {
		complexity++;
		super.visitDoWhileStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitWhileStatement(org.sonar.plugins.java.api.tree.WhileStatementTree)
	 */
	@Override
	public void visitWhileStatement(WhileStatementTree tree) {
		complexity++;
		super.visitWhileStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitReturnStatement(org.sonar.plugins.java.api.tree.ReturnStatementTree)
	 */
	@Override
	public void visitReturnStatement(ReturnStatementTree tree) {
		complexity++;
		super.visitReturnStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitThrowStatement(org.sonar.plugins.java.api.tree.ThrowStatementTree)
	 */
	@Override
	public void visitThrowStatement(ThrowStatementTree tree) {
		complexity++;
		super.visitThrowStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitCatch(org.sonar.plugins.java.api.tree.CatchTree)
	 */
	@Override
	public void visitCatch(CatchTree tree) {
		complexity++;
		super.visitCatch(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitConditionalExpression(org.sonar.plugins.java.api.tree.ConditionalExpressionTree)
	 */
	@Override
	public void visitConditionalExpression(ConditionalExpressionTree tree) {
		complexity++;
		super.visitConditionalExpression(tree);
	}
}
