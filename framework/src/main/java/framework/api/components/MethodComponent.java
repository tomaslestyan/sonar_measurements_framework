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
	 * Constructor
	 * Use {@link Builder} to create this class
	 * @param id
	 * @param sonarComponentID
	 * @param parentClass
	 * @param measures
	 * @param endLine 
	 * @param startLine 
	 */
	MethodComponent(String id, String sonarComponentID, String parentClass, Map<String, Integer> measures, int startLine, int endLine) {
		super(id, sonarComponentID, parentClass, Scope.METHOD, null, measures, startLine, endLine);
	}

	/**
	 * @return the builder of {@link MethodComponent} class
	 */
	public static Builder builder() {
		return new Builder(Scope.METHOD);
	}
}
