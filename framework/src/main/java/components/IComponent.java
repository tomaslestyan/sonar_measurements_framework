/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.components;

import java.util.Collection;
import java.util.Map;

/**
 * Custom component interface
 * @author Tomas
 */
public interface IComponent {

	/**
	 * @return ID of the sonar FILE component
	 */
	String getSonarComponentID();

	/**
	 * @return the unique ID of the component
	 */
	String getID();

	/**
	 * @return the parent class of the component (not super class nor interface) in which it is located, null for regular classes
	 */
	String getParent();

	/**
	 * @return Collection of child components, e.g., nested classes, anonymous classes or methods
	 */
	Collection<IComponent> getChildComponents();

	/**
	 * @return the measures of the component (key: metric, value: measure for the metric)
	 * FIXME <Object, Object> is only for evaluation purpose, should be changed into something reasonable
	 */
	Map<Object, Object> getMeasures();

}
