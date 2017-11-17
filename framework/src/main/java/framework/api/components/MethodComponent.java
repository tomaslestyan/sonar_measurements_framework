/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api.components;

import java.util.Map;

import main.java.framework.api.Scope;

/**
 * Method component
 * @author Tomas
 */
public class MethodComponent extends AComponent {

	/**
	 * Stores return type of the method
	 */
	private String returnType;

	/**
	 * Constructor
	 * Use {@link Builder} to create this class
	 * @param id
	 * @param sonarComponentID
	 * @param parentClass
	 * @param measures
	 * @param endLine 
	 * @param startLine 
	 */
	MethodComponent(String id, String sonarComponentID, String fileKey, String sonarFileKey, String parentClass, String returnType, Map<String, Integer> measures, int startLine, int endLine) {
		super(id, sonarComponentID, fileKey, sonarFileKey, parentClass, Scope.METHOD,null, measures, startLine, endLine);
		this.returnType = returnType;
	}

	/**
	 *
	 * @return the return type of the method as String
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * @return the builder of {@link MethodComponent} class
	 */
	public static Builder builder() {
		return new Builder(Scope.METHOD);
	}
}
