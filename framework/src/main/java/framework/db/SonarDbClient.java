/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;

import main.java.framework.api.components.ClassComponent;
import main.java.framework.api.components.IComponent;
import main.java.framework.api.components.MethodComponent;

/**
 * Client to access the Sonarqube database
 *
 * @author Tomas Lestyan
 * @author Klara Erlebachova
 */
public class SonarDbClient {

	/** The logger object */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private HikariDataSource dataSource;

	private static final String SELECT_ALL_COMPONENTS = "SELECT * FROM Measurement_Framework_Components";
	private static final String SELECT_COMPONENT = "SELECT * FROM Measurement_Framework_Components WHERE id = ?";
	private static final String SELECT_COMPONENTS_BY_PARENT = "SELECT * FROM Measurement_Framework_Components WHERE parent = ?;";
	private static final String SELECT_RECENT_MEASURES_FOR_COMPONENT = "SELECT * FROM Measurement_Framework_Recent_Measures WHERE Componentsid = ?;";
	private static final String SELECT_MEASURES_FOR_METRIC = "SELECT * FROM Measurement_Framework_Measures WHERE Metricsid = ?;";
	private static final String SELECT_RECENT_MEASURES_FOR_METRIC = "SELECT * Measurement_Framework_Recent_Measures WHERE Metricsid = ?;";
	private static final String SELECT_CHILD_CLASSES = "SELECT * FROM Measurement_Framework_Components WHERE superclass = ? AND type = 1";
	private static final String SELECT_ROOT_CLASSES = "SELECT * FROM Measurement_Framework_Components WHERE (superclass IS NULL OR superclass NOT IN (SELECT fullyQualifiedName FROM Measurement_Framework_Components WHERE projectKey = ?)) AND TYPE = 1 AND projectKey = ?";
	private static final String SELECT_CLASSES_FOR_PROJECT = "SELECT * FROM Measurement_Framework_Components WHERE TYPE = 1 AND parent IS NULL AND projectKey = ?";
	private static final String SELECT_BOUNDARIES_FOR_METRIC = "SELECT min(m.value) as min_value, max(m.value) as max_value FROM measurement_framework_recent_measures m " +
			"JOIN measurement_framework_components c on (m.componentsid = c.id) " +
			"WHERE c.projectkey = ? and m.metricsid = ? and c.type = 1";

	/**
	 * Constructor
	 * @param dataSource
	 */
	public SonarDbClient(HikariDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Get components from components table in embedded DB.
	 *
	 * @param parent the parent component, for all components it should be <code>null</code>
	 * @return collections of components
	 */
	public Collection<IComponent> getComponents(String parent) {
		long queryStart = System.currentTimeMillis();
		Collection<IComponent> components = new ArrayList<>();
		String selectSql = parent == null ? SELECT_ALL_COMPONENTS : SELECT_COMPONENTS_BY_PARENT;
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement selectComponents = connection.prepareStatement(selectSql)) {
				if (parent != null) {
					selectComponents.setString(1, parent);
				}
				try (ResultSet queryResult = selectComponents.executeQuery()) {
					log.info("Component Query took" + (System.currentTimeMillis() - queryStart) + " ms");
					while (queryResult.next()) {
						long startTime = System.currentTimeMillis();
						IComponent component = parseComponentFromQuery(queryResult);
						if (component != null) {
							components.add(component);
						}
						long time = System.currentTimeMillis() - startTime;
						log.info("Retrieval of component " + component.getID() + "took " + time + " ms");
					}
				}
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve components", e);
		}
		return components;
	}

	/** Get classes from classes for project in tree hierarchy
	 * @param projectKey
	 * @return collections of components
	 */
	public Collection<ClassComponent> getClassComponentsOfProject(String projectKey) {
		Collection<ClassComponent> components = new ArrayList<>();
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement findClasses = connection.prepareStatement(SELECT_CLASSES_FOR_PROJECT)) {
				findClasses.setString(1, projectKey);
				try (ResultSet queryResult = findClasses.executeQuery()) {
					while (queryResult.next()) {
						ClassComponent component = parseClassFromQuery(queryResult);
						if (component != null) {
							components.add(component);
						}
					}
				}
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve components", e);
		}
		return components;
	}

	/** Get component from DB
	 * @param id the unique ID of the component
	 * @return component with given ID, or <code>null</code> if not found
	 */
	public IComponent getComponent(String id) {
		IComponent component = null;
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement selectComponents = connection.prepareStatement(SELECT_COMPONENT)) {
				selectComponents.setString(1, id);
				try (ResultSet queryResult = selectComponents.executeQuery()) {
					while (queryResult.next()) {
						component = parseComponentFromQuery(queryResult);
					}
				}
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve components", e);
		}
		return component;
	}

	/**
	 * Parse component from query result
	 *
	 * @param queryResult
	 * @throws SQLException
	 */
	private IComponent parseComponentFromQuery(ResultSet queryResult) throws SQLException {
		String type = queryResult.getString("TYPE");
		String id = queryResult.getString("ID");
		String sonarKey = queryResult.getString("projectKey");
		String fileKey = queryResult.getString("fileKey");
		String parentID = queryResult.getString("parent");
		String packageName = queryResult.getString("package");
		String superclass = queryResult.getString("superclass");
		String interfaces = queryResult.getString("interfaces");
		int start = queryResult.getInt("STARTLINE");
		int end = queryResult.getInt("ENDLINE");
		Map<String, Integer> measures = getRecentMeasuresForComponent(id);
		if (type.equalsIgnoreCase("2")) {
			return MethodComponent.builder()
					.setId(id)
					.setSonarProjectID(sonarKey)
					.setFileKey(fileKey)
					.setParentClass(parentID)
					.setMeasures(measures)
					.setStartLine(start)
					.setEndLine(end)
					.build();
		} else if (type.equals("1")) {
			return ClassComponent.builder()
					.setId(id)
					.setSonarProjectID(sonarKey)
					.setFileKey(fileKey)
					.setParentClass(parentID)
					.setMeasures(measures)
					.setChildren(getComponents(id))
					.setPackageName(packageName)
					.setSuperClass(superclass)
					.setInterfaces(Lists.newArrayList(Splitter.on(",").split(interfaces)))
					.setStartLine(start)
					.setEndLine(end)
					.build();
		}
		log.warn("Incorrect component type of component with ID = " + id);
		return null;
	}

	/**
	 * Get recent measures (from last scan) for given component
	 *
	 * @param id if of the component
	 * @return measures
	 */
	private Map<String, Integer> getRecentMeasuresForComponent(String id) {
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement selectMeasures = connection.prepareStatement(SELECT_RECENT_MEASURES_FOR_COMPONENT)) {
				Map<String, Integer> measures = new HashMap<>();
				selectMeasures.setString(1, id);
				try (ResultSet queryResult = selectMeasures.executeQuery()) {
					while (queryResult.next()) {
						String metric = queryResult.getString("Metricsid");
						int value = queryResult.getInt("value");
						measures.put(metric, Integer.valueOf(value));
					}
				}
				return measures;
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve recent measures", e);
		}
		return null;
	}

	/**
	 * Get measures for given metric
	 * @param metric if of the metric
	 * @return measures
	 */
	public List<Integer> getMeasures(String metric) {
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement selectMeasures = connection.prepareStatement(SELECT_MEASURES_FOR_METRIC)) {
				List<Integer> measures = new ArrayList<>();
				selectMeasures.setString(1, metric);
				try (ResultSet queryResult = selectMeasures.executeQuery()) {
					while (queryResult.next()) {
						int value = queryResult.getInt("value");
						measures.add(Integer.valueOf(value));
					}
				}
				return measures;
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve measures", e);
		}
		return null;
	}

	/** Get classes from classes for project in tree hierarchy
	 * @param projectKey
	 * @return collections of components
	 */
	public Collection<ClassComponent> getRootClasses(String projectKey) {
		Collection<ClassComponent> components = new ArrayList<>();
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement findRootClasses = connection.prepareStatement(SELECT_ROOT_CLASSES)) {
				findRootClasses.setString(1, projectKey);
				findRootClasses.setString(2, projectKey);
				try (ResultSet queryResult = findRootClasses.executeQuery()) {
					while (queryResult.next()) {
						ClassComponent component = parseClassFromQuery(queryResult);
						if (component != null) {
							components.add(component);
						}
					}
				}
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve components", e);
		}
		return components;
	}

	/**
	 * Parse class from query result
	 * @param queryResult
	 * @throws SQLException
	 */
	private ClassComponent parseClassFromQuery(ResultSet queryResult) throws SQLException {
		String id = queryResult.getString("ID");
		String sonarKey = queryResult.getString("projectKey");
		String fileKey = queryResult.getString("fileKey");
		String parentID = queryResult.getString("parent");
		String packageName = queryResult.getString("package");
		String fullyQualifiedName = queryResult.getString("fullyQualifiedName");
        String superclass = queryResult.getString("superclass");
		String interfaces = queryResult.getString("interfaces");
		int start = queryResult.getInt("STARTLINE");
		int end = queryResult.getInt("ENDLINE");
		Map<String, Integer> measures = getRecentMeasuresForComponent(id);

		return (ClassComponent) ClassComponent.builder()
				.setId(id)
				.setSonarProjectID(sonarKey)
				.setFileKey(fileKey)
				.setParentClass(parentID)
				.setMeasures(measures)
				.setChildrenClasses(getChildClassesFor(fullyQualifiedName))
				.setPackageName(packageName)
				.setSuperClass(superclass)
				.setInterfaces(Lists.newArrayList(Splitter.on(",").split(interfaces)))
				.setStartLine(start)
				.setEndLine(end)
				.build();
	}

	/** Get child classes for superClass
	 * @param superClass the superClass
	 * @return collections of components
	 */
	private Collection<ClassComponent> getChildClassesFor(String superClass) {
		Collection<ClassComponent> components = new ArrayList<>();
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement findClasses = connection.prepareStatement(SELECT_CHILD_CLASSES)) {
				findClasses.setString(1, superClass);
				try (ResultSet queryResult = findClasses.executeQuery()) {
					while (queryResult.next()) {
						ClassComponent component = parseClassFromQuery(queryResult);
						if (component != null) {
							components.add(component);
						}
					}
				}
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve components", e);
		}
		return components;
	}

	/**
	 * Get measures for given metric
	 * @param metricID id of the metric
	 * @return recent measures
	 */
	public List<Integer> getRecentMeasures(String metricID) {
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement selectMeasures = connection.prepareStatement(SELECT_RECENT_MEASURES_FOR_METRIC)) {
				List<Integer> recentMeasures = new ArrayList<>();
				selectMeasures.setString(1, metricID);
				try (ResultSet queryResult = selectMeasures.executeQuery()) {
					while (queryResult.next()) {
						int value = queryResult.getInt("value");
						recentMeasures.add(Integer.valueOf(value));
					}
				}
				return recentMeasures;
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve recent measures", e);
		}
		return null;
	}

	public Pair<Integer, Integer> getBoundariesForMetric(String projectKey, String metric) {
		try (Connection connection = this.dataSource.getConnection()) {
			try (PreparedStatement selectBoundaries = connection.prepareStatement(SELECT_BOUNDARIES_FOR_METRIC)) {
				selectBoundaries.setString(1, projectKey);
				selectBoundaries.setString(2, metric);
				try (ResultSet queryResult = selectBoundaries.executeQuery()) {
					if (queryResult.next()) {
						int minValue = queryResult.getInt("min_value");
						int maxValue = queryResult.getInt("max_value");
						return new ImmutablePair<>(minValue, maxValue);
					}
				}
			}
		} catch (SQLException e) {
			log.warn("Can't retrieve boundaries for metric", e);
		}
		return null;
	}
}
