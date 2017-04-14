/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import main.java.visitors.Scope;

/**
 * Builder class for {@link IComponent}
 * @author Tomas
 */
public class Builder {

	/** Type of the component*/
	private Scope type;
	/** Unique ID of the component*/
	private String id;
	/** ID of the sonar FILE component */
	private String sonarComponentID;
	/** The parent class of the component (not super class nor interface) in which it is located, null for regular classes */
	private String parentClass;
	/** Collection of child components, e.g., nested classes, anonymous classes or methods  */
	private Collection<IComponent> children;
	/** Measures of the component (key: metric, value: measure for the metric)  */
	private Map<String, Integer> measures = new HashMap<>();
	/** The name of the package */
	private String packageName;
	/** The super class of the class component, for other components should be <code>null</code> */
	private String superClass;
	/** The list of interface classes of the class component, for other components should be <code>null</code> or empty */
	private Collection<String> interfaces;
	/** The starting line of the component */
	private int startLine;
	/** The ending line of the component */
	private int endLine;

	/**
	 * Constructor
	 * @param type
	 */
	public Builder(Scope type) {
		this.type = type;
	}

	/**
	 * @param id the id to set
	 * @return the instance of the builder
	 */
	public Builder setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * @param sonarComponentID the sonarComponentID to set
	 * @return the instance of the builder
	 */
	public Builder setSonarComponentID(String sonarComponentID) {
		this.sonarComponentID = sonarComponentID;
		return this;
	}

	/**
	 * @param parentClass the parentClass to set
	 * @return the instance of the builder
	 */
	public Builder setParentClass(String parentClass) {
		this.parentClass = parentClass;
		return this;
	}

	/**
	 * @param children the children to set
	 * @return the instance of the builder
	 */
	public Builder setChildren(Collection<IComponent> children) {
		this.children = children;
		return this;
	}

	/**
	 * @param measures the measures to set
	 * @return the instance of the builder
	 */
	public Builder setMeasures(Map<String, Integer> measures) {
		this.measures = measures;
		return this;
	}

	/**
	 * @param packageName the packageName to set
	 * @return the instance of the builder
	 */
	public Builder setPackageName(String packageName) {
		this.packageName = packageName;
		return this;
	}

	/**
	 * @param superClass the superClass to set
	 * @return the instance of the builder
	 */
	public Builder setSuperClass(String superClass) {
		this.superClass = superClass;
		return this;
	}

	/**
	 * @param interfaces the interfaces to set
	 * @return the instance of the builder
	 */
	public Builder setInterfaces(Collection<String> interfaces) {
		this.interfaces = interfaces;
		return this;
	}

	/**
	 * @param startLine the startLine to set
	 * @return the instance of the builder
	 */
	public Builder setStartLine(int startLine) {
		this.startLine = startLine;
		return this;
	}

	/**
	 * @param endLine the endLine to set
	 * @return the instance of the builder
	 */
	public Builder setEndLine(int endLine) {
		this.endLine = endLine;
		return this;
	}

	/**
	 * @return builded instance of {@link IComponent} or <code>null</code> if something missing
	 */
	public IComponent build() {
		IComponent component = null;
		switch (type) {
		case METHOD:
			// TODO check required parameters
			component =new MethodComponent(id, sonarComponentID, parentClass, measures, startLine, endLine);
			break;
		case CLASS:
			// TODO check required parameters
			component =new ClassComponent(id, sonarComponentID, packageName, parentClass, superClass, interfaces, children, measures, startLine, endLine);
			break;
		default:
			component = null;
			break;
		} 
		return component;
	}
}
