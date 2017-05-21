/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.visitors.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import main.java.framework.api.Scope;

/**
 * Visitor for Tight Class Cohesion metric
 * @author Tomas
 */
public class TightClassCohesionVisitor extends AVisitor {

	/** Attributes of parent class */
	private Collection<String> attributes = new HashSet<>();
	/** Map of methods (keys) and attributes (values) which they are accessing */
	private Map<String, Collection<String>> methodAccesses = new HashMap<>();
	/** Simple name of the parent class */
	private String className = null;

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getKey()
	 */
	@Override
	public String getKey() {
		return "tcc";
	}

	/* (non-Javadoc)
	 * @see main.java.framework.api.ICommonVisitor#getScope()
	 */
	@Override
	public Scope getScope() {
		return Scope.CLASS;
	}

	/* (non-Javadoc)
	 * @see main.java.framework.visitors.java.AVisitor#scanTree(org.sonar.plugins.java.api.tree.Tree)
	 */
	@Override
	public void scanTree(Tree tree) {
		this.attributes = new HashSet<>();
		this.methodAccesses = new HashMap<>();
		super.scanTree(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitClass(org.sonar.plugins.java.api.tree.ClassTree)
	 */
	@Override
	public void visitClass(ClassTree tree) {
		this.className = tree.simpleName().name();
		super.visitClass(tree);
	}


	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		MethodAttributeAccesVisitor accesedAttributesVissitor = new MethodAttributeAccesVisitor(attributes);
		tree.accept(accesedAttributesVissitor);
		String methodName = accesedAttributesVissitor.getMethodName();
		Collection<String> accessedAttributes = accesedAttributesVissitor.getAccessedAttributes();
		// exclude constructors
		if (!Objects.equals(className, methodName)) {			
			methodAccesses.put(methodName, accessedAttributes);
		} 
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitVariable(org.sonar.plugins.java.api.tree.VariableTree)
	 */
	@Override
	public void visitVariable(VariableTree tree) {
		attributes.add(tree.simpleName().name());
	}

	/* (non-Javadoc)
	 * @see main.java.framework.visitors.java.AVisitor#getResult()
	 */
	@Override
	public int getResult() {
		Map<String, Collection<String>> methodAccessesToCompare = new HashMap<>(methodAccesses);
		methodAccesses.forEach((k, v) -> {
			methodAccessesToCompare.remove(k);
			if (!methodAccessesToCompare.isEmpty()) {
				computePairs(v, methodAccessesToCompare);				
			}
		});
		return super.getResult();
	}

	/**
	 * Compute number of pairs of methods accessing at least one same attribute of the class
	 * @param methodName
	 * @param accesses
	 * @param methodAccessesToCompare
	 */
	private void computePairs(Collection<String> accesses, Map<String, Collection<String>> methodAccessesToCompare) {
		methodAccessesToCompare.forEach((k, v) -> {
			long matches = v.stream().filter(accesses::contains).count();
			if (matches > 0) {
				count++;
			}
		});

	}

}
