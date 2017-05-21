/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.java.metricvisitors;

import java.util.Collection;
import java.util.HashSet;

import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;

/**
 * Helper visitor for {@link TightClassCohesionVisitor}
 * @author Tomas
 */
public class MethodAttributeAccesVisitor extends BaseTreeVisitor {

	/** The name of the method */
	private String methodName = null;
	/** Accessed attributes of parent class */
	private Collection<String> accessedAttributes = new HashSet<>();
	/** Attributes of parent class */
	private Collection<String> classAttributes;

	/**
	 * Constructor
	 * @param classAttributes
	 */
	public MethodAttributeAccesVisitor(Collection<String> classAttributes) {
		this.classAttributes = classAttributes;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the accessedAttributes
	 */
	public Collection<String> getAccessedAttributes() {
		return accessedAttributes;
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitMethod(org.sonar.plugins.java.api.tree.MethodTree)
	 */
	@Override
	public void visitMethod(MethodTree tree) {
		this.methodName = tree.simpleName().name();
		super.visitMethod(tree);
	}

	/* (non-Javadoc)
	 * @see org.sonar.plugins.java.api.tree.BaseTreeVisitor#visitIdentifier(org.sonar.plugins.java.api.tree.IdentifierTree)
	 */
	@Override
	public void visitIdentifier(IdentifierTree tree) {
		String identifierName = tree.name();
		if (classAttributes.contains(identifierName)) {
			accessedAttributes.add(identifierName);
		}
	}
}
