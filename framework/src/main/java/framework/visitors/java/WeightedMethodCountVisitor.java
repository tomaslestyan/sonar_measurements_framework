/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.visitors.java;

import org.sonar.plugins.java.api.tree.MethodTree;

import main.java.framework.api.Scope;

/**
 * Visitor of Weighted Method Count metric
 * @author Tomas
 */
public class WeightedMethodCountVisitor extends AVisitor {

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getKey()
	 */
	@Override
	public String getKey() {
		return "wmc";
	}

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getScope()
	 */
	@Override
	public Scope getScope() {
		return Scope.CLASS;
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		ComplexityVisitor cycloVysitor = new ComplexityVisitor();
		cycloVysitor.scanTree(tree);
		count += cycloVysitor.getResult();
	}

}
