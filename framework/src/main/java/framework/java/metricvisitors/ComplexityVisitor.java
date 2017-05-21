/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.framework.java.metricvisitors;

import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.ConditionalExpressionTree;
import org.sonar.plugins.java.api.tree.DoWhileStatementTree;
import org.sonar.plugins.java.api.tree.ForEachStatement;
import org.sonar.plugins.java.api.tree.ForStatementTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.ReturnStatementTree;
import org.sonar.plugins.java.api.tree.ThrowStatementTree;
import org.sonar.plugins.java.api.tree.WhileStatementTree;

import main.java.framework.api.Scope;

/**
 * Cyclomatic complexity visitor
 * @author Tomas Lestyan
 */
public class ComplexityVisitor extends AVisitor {

	public static final String KEY = "cyclo";

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
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitCaseLabel(org.sonar.plugins.java.api.tree.CaseLabelTree)
	 */
	@Override
	public void visitCaseLabel(CaseLabelTree tree) {
		if (!"default".equals(tree.caseOrDefaultKeyword().text())) {
			count++;
		}
		super.visitCaseLabel(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitIfStatement(org.sonar.plugins.java.api.tree.IfStatementTree)
	 */
	@Override
	public void visitIfStatement(IfStatementTree tree) {
		count++;
		super.visitIfStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitForStatement(org.sonar.plugins.java.api.tree.ForStatementTree)
	 */
	@Override
	public void visitForStatement(ForStatementTree tree) {
		count++;
		super.visitForStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitForEachStatement(org.sonar.plugins.java.api.tree.ForEachStatement)
	 */
	@Override
	public void visitForEachStatement(ForEachStatement tree) {
		count++;
		super.visitForEachStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitDoWhileStatement(org.sonar.plugins.java.api.tree.DoWhileStatementTree)
	 */
	@Override
	public void visitDoWhileStatement(DoWhileStatementTree tree) {
		count++;
		super.visitDoWhileStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitWhileStatement(org.sonar.plugins.java.api.tree.WhileStatementTree)
	 */
	@Override
	public void visitWhileStatement(WhileStatementTree tree) {
		count++;
		super.visitWhileStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitReturnStatement(org.sonar.plugins.java.api.tree.ReturnStatementTree)
	 */
	@Override
	public void visitReturnStatement(ReturnStatementTree tree) {
		count++;
		super.visitReturnStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitThrowStatement(org.sonar.plugins.java.api.tree.ThrowStatementTree)
	 */
	@Override
	public void visitThrowStatement(ThrowStatementTree tree) {
		count++;
		super.visitThrowStatement(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitCatch(org.sonar.plugins.java.api.tree.CatchTree)
	 */
	@Override
	public void visitCatch(CatchTree tree) {
		count++;
		super.visitCatch(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitConditionalExpression(org.sonar.plugins.java.api.tree.ConditionalExpressionTree)
	 */
	@Override
	public void visitConditionalExpression(ConditionalExpressionTree tree) {
		count++;
		super.visitConditionalExpression(tree);
	}
}
