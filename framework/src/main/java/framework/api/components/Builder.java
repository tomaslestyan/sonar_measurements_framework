/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sonar.plugins.java.api.JavaFileScannerContext;

import main.java.framework.api.Scope;

/**
 * Builder class for {@link IComponent}
 * @author Tomas
 */
public class Builder {

	/** Type of the component*/
	private Scope type;
	/** Unique ID of the component*/
	private String id;
	/** ID of the sonar PROJECT component */
	private String sonarComponentID;
	/** Key of the owner file */
	private String fileKey;
	/** Key of the owner file defined by SonarQube */
	private String sonarfileKey;
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
	/** The list of children of the class component */
	private Collection<ClassComponent> childrenClasses;
	/**Decides if class is an interface**/
	private boolean isInterface;

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
	 * @param sonarComponentID the sonarQube Project ID to set
	 * @return the instance of the builder
	 */
	public Builder setSonarProjectID(String sonarComponentID) {
		this.sonarComponentID = sonarComponentID;
		return this;
	}

	/**
	 * @param fileKey the key of the file of the component as specified in {@link JavaFileScannerContext#getFileKey()}
	 * @return the instance of the builder
	 */
	public Builder setFileKey(String fileKey) {
		this.fileKey = fileKey;
		return this;
	}

	/**
	 * @param sonarfileKey the sonarfileKey to set
	 * @return the instance of the builder
	 */
	public Builder setSonarfileKey(String sonarfileKey) {
		this.sonarfileKey = sonarfileKey;
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

	public Builder setIsInterface(boolean isInterface){
		this.isInterface = isInterface;
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
	 * @param childrenClasses
	 * @return
	 */
	public Builder setChildrenClasses(Collection<ClassComponent> childrenClasses) {
		this.childrenClasses = childrenClasses;
		return this;
	}

	/**
	 * @return builded instance of {@link IComponent} or <code>null</code> if something missing
	 */
	public IComponent build() {
		IComponent component;
		switch (type) {
		case METHOD:
			component = new MethodComponent(id, sonarComponentID, fileKey, sonarfileKey, parentClass, measures, startLine, endLine);
			break;
		case CLASS:
			component =new ClassComponent(id, sonarComponentID, fileKey, sonarfileKey, packageName, parentClass, superClass, interfaces, children, measures, isInterface, startLine, endLine, childrenClasses);
			break;
		default:
			component = null;
			break;
		} 
		return component;
	}
}
