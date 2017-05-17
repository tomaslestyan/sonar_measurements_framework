/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import main.java.framework.api.components.ClassComponent;
import main.java.framework.api.components.IComponent;
import main.java.framework.db.DataSourceProvider;
import main.java.framework.db.SonarDbClient;

/**
 * Class for retrieving information from Sonar DB
 * @author Tomas
 */
public class MeasurementRepository {

	private MeasurementRepository() {
		// do not allow to create instances
	}

	/**
	 * @return collection of components, null if connection failed (check the log in that case)
	 */
	public static Collection<IComponent> getComponents() {
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		return client.getComponents(null);
	}

	/**
	 * @param projectKey 
	 * @return collection of Class components, null if connection failed (check the log in that case)
	 */
	public static Collection<ClassComponent> getClassComponents(String projectKey) {
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		return client.getClassComponentsOfProject(projectKey);
	}

	/**
	 * @param id
	 * @return component with given id if such component exists, <code>null</code> otherwise
	 */
	public static IComponent getComponent(String id) {
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		return client.getComponent(id);
	}

	/**
	 * @param projectKee 
	 * @return collection of Class components in tree hierarchy, null if connection failed (check the log in that case)
	 */
	public static Collection<ClassComponent> getTreeOfClassComponents(String projectKee) {
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		return client.getRootClasses(projectKee);
	}

	/**
	 * @param metric the ID of the metric
	 * @return long term measures of the given metric
	 */
	public static List<Integer> getMeasures(String metric) {
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		return client.getMeasures(metric);
	}

	/**
	 * @param metric the ID of the metric
	 * @return recent measures of the given metric
	 */
	public static List<Integer> getRecentMeasures(String metric) {
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		return client.getRecentMeasures(metric);
	}

	/**
	 * @param metrics
	 * @return
	 */
	public static Map<String, List<Integer>> getMeasures(List<String> metrics) {
		Map<String, List<Integer>> metricsMeasures = new HashMap<>();
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		metrics.forEach(x -> metricsMeasures.put(x, client.getMeasures(x)));
		return metricsMeasures;
	}

	/**
	 * @param metrics
	 * @return
	 */
	public static Map<String, List<Integer>> getRecentMeasures(List<String> metrics) {
		Map<String, List<Integer>> recentMetricsMeasures = new HashMap<>();
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		metrics.forEach(x -> recentMetricsMeasures.put(x, client.getRecentMeasures(x)));
		return recentMetricsMeasures;
	}

	public static Pair<Integer, Integer> getBoundariesFor(String projectKey, String metric) {
		SonarDbClient client = new SonarDbClient(DataSourceProvider.getDataSource());
		return client.getBoundariesForMetric(projectKey, metric);
	}
}
