/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import main.java.framework.api.components.ClassComponent;
import main.java.framework.api.components.IComponent;
import main.java.framework.db.SonarDbClient;

/**
 * Class for retrieving information from Sonar DB
 * @author Tomas
 */
public class Database {

	private Database() {
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

	/**
	 * @param metric the ID of the metric
	 * @return long term measures of the given metric
	 */
	public static List<Integer> getMeasures(String metric) {
		SonarDbClient client = new SonarDbClient(true);
		List<Integer> measures = client.getMeasures(metric);
		client.disconnect();
		return measures;
	}

	/**
	 * @param metrics
	 * @return
	 */
	public static Map<String, List<Integer>> getMeasures(List<String> metrics) {
		Map<String, List<Integer>> metricsMeasures = new HashMap<>();
		SonarDbClient client = new SonarDbClient(true);
		metrics.forEach(x -> metricsMeasures.put(x, client.getMeasures(x)));
		client.disconnect();
		return metricsMeasures;
	}


}
