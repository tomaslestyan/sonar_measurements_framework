/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.java.metricvisitors;

import java.util.HashSet;
import java.util.Set;

import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import main.java.framework.api.Scope;

/**
 * Visitor for ATFD CLASS metric
 * @author Tomas
 */
public class AtfdClassVisitor extends AVisitor{

	Set<String> providers = new HashSet<>();

	@Override
	public void scanTree(Tree tree) {
		this.providers = new HashSet<>();
		super.scanTree(tree);
	}

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getKey()
	 */
	@Override
	public String getKey() {
		return "atfd_class";
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
		DataAccessVisitor dataAccessVisitor = new DataAccessVisitor(true);
		tree.accept(dataAccessVisitor);
		providers.addAll(dataAccessVisitor.getProviders());
	}

	/* (non-Javadoc)
	 * @see main.java.framework.java.metricvisitors.AVisitor#getResult()
	 */
	@Override
	public int getResult() {
		return providers.size();
	}

}
