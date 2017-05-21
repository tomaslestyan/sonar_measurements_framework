/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api.components;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import main.java.framework.api.Scope;

/**
 * Class component
 * @author Tomas
 */
public class ClassComponent extends AComponent {

	/** The name of the package */
	private String packageName;
	/** The super class of the class component, for other components should be <code>null</code> */
	private String superClass;
	/** The list of interface classes of the class component, for other components should be <code>null</code> or empty */
	private Collection<String> interfaces;
	/** The list of children of the class component */
	private Collection<ClassComponent> children;
	/**Decides if class is an interface, or not**/
	private boolean isInterface;


	/**
	 * Constructor
	 * Use {@link Builder} to create this class
	 * @param id
	 * @param sonarComponentID
	 * @param packageName 
	 * @param parentClass
	 * @param interfaces 
	 * @param superClass 
	 * @param parentClass 
	 * @param children
	 * @param measures
	 * @param endLine 
	 * @param startLine 
	 */
	ClassComponent(String id, String sonarComponentID, String fileKey, String sonarFileKey, String packageName, String parentClass, String superClass, Collection<String> interfaces, Collection<IComponent> children,
			Map<String, Integer> measures, boolean isInterface, int startLine, int endLine, Collection<ClassComponent> childrenClasses) {
		super(id, sonarComponentID, fileKey, sonarFileKey, parentClass, Scope.CLASS, children, measures, startLine, endLine);

		this.interfaces = (interfaces == null) ? Collections.emptyList() : interfaces;
		this.superClass = superClass;
		this.packageName = packageName;
		this.children = childrenClasses;
		this.isInterface = isInterface;

	}

	/**
	 * @return the Builder of {@link ClassComponent} class
	 */
	public static Builder builder() {
		return new Builder(Scope.CLASS);
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return the superClass
	 */
	public String getSuperClass() {
		return superClass;
	}

	/**
	 * @return the interfaces
	 */
	public Collection<String> getInterfaces() {
		return interfaces;
	}


	public Collection<ClassComponent> getChildren() {
		return children;
	}

	public boolean isInterface() { return isInterface; }
}
