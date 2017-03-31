/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.components;

import java.util.Map;

/**
 * Method component
 * @author Tomas
 */
public class MethodComponent extends AComponent {

	/**
	 * Constructor
	 * @param id
	 * @param sonarComponentID
	 * @param parentClass
	 * @param measures
	 */
	public MethodComponent(String id, String sonarComponentID, String parentClass, Map<Object, Object> measures) {
		super(id, sonarComponentID, parentClass, null, measures);
	}
}
