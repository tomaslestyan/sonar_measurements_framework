/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api.components;

import java.util.Collection;
import java.util.stream.Collectors;

import main.java.db.SonarDbClient;

/**
 * Class for retrieving stored components from Sonar DB
 * @author Tomas
 */
public class ComponentFactory {

	private ComponentFactory() {
		// do not allow to create instances
	}

	/**
	 * @return collection of components, null if connection failed (check the log in that case)
	 */
	public static Collection<IComponent> getComponents() {
		SonarDbClient client = new SonarDbClient(true);
		Collection<IComponent> components = client.getComponents(null);
		client.disconnect();
		return components;
	}

	/**
	 * @return collection of Class components, null if connection failed (check the log in that case)
	 */
	public static Collection<IComponent> getClassComponents() {
		return getComponents().stream().filter(x -> x instanceof ClassComponent).collect(Collectors.toList());
	}
}
