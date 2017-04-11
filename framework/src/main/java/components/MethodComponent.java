/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.components;

import java.util.Map;

import main.java.visitors.Scope;

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
	MethodComponent(String id, String sonarComponentID, String parentClass, Map<Object, Object> measures, int startLine, int endLine) {
		super(id, sonarComponentID, parentClass, null, measures, startLine, endLine);
	}

	/**
	 * @return the builder of {@link MethodComponent} class
	 */
	public static Builder builder() {
		return new Builder(Scope.METHOD);
	}
}
