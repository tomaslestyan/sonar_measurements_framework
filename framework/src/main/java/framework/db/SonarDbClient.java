/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.measures.Metric;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import main.java.framework.api.components.ClassComponent;
import main.java.framework.api.components.IComponent;
import main.java.framework.api.components.MethodComponent;

/**
 * Client to access the SonarQube H2 database
 *
 * @author Tomas Lestyan
 * @author Klara Erlebachova
 */
public class SonarDbClient {

   /** The logger object */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private HikariDataSource dataSource;

    private static final String FIND_COMPONENT = "SELECT * FROM Measurement_Framework_Components WHERE id = ?;";
    private static final String INSERT_COMPONENT = "INSERT INTO Measurement_Framework_Components (id , projectKey, fileKey, parent, type, package, superClass, interfaces, startLine, endLine) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String UPDATE_COMPONENT = "UPDATE Measurement_Framework_Components SET projectKey = ?, fileKey = ?, parent = ?, type = ?, package = ?, superClass = ?, interfaces = ?, startLine = ?, endLine = ? WHERE id = ?;";

    private static final String SAVE_MEASURE = "INSERT INTO Measurement_Framework_Recent_Measures (id, value , Componentsid, Metricsid) VALUES (?, ?, ?, ?)";

    private static final String COPY_RECENT_MEASURES_TO_MEASURES = "INSERT INTO Measurement_Framework_Measures (id, value , Componentsid, Metricsid) " +
            "SELECT id, value , Componentsid, Metricsid FROM Measurement_Framework_Recent_Measures " +
            "WHERE id NOT IN (SELECT id FROM Measurement_Framework_Measures);";
    private static final String EMPTY_RECENT_MEASURES = "DELETE FROM Measurement_Framework_Recent_Measures;";

    private static final String SELECT_ALL_COMPONENTS = "SELECT * FROM Measurement_Framework_Components;";
    private static final String SELECT_COMPONENTS_BY_PARENT = "SELECT * FROM Measurement_Framework_Components WHERE parent = ?;";
    private static final String SELECT_RECENT_MEASURES_FOR_COMPONENT = "SELECT * FROM Measurement_Framework_Recent_Measures WHERE Componentsid = ?;";
    private static final String SELECT_MEASURES_FOR_METRIC = "SELECT * FROM Measurement_Framework_Measures WHERE Metricsid = ?;";

    /**
     * Constructor
     */
    public SonarDbClient(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Create mandatory tables in Sonar DB if they were not created yet.
     *
     * @return <code>true</code> if tables was created or the were created before, <code>false</code> otherwise
     */
    public boolean saveRecentMeasuresToMeasures() {
        try (Connection connection = this.dataSource.getConnection()) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(
                        COPY_RECENT_MEASURES_TO_MEASURES +
                                EMPTY_RECENT_MEASURES);
            }
        } catch (SQLException e) {
            log.warn("Can't save recent measures to measures", e);
            return false;
        }
        return true;
    }

    /**
     * Save component into DB
     *
     * @param id
     * @param fileID
     * @param project     TODO
     * @param parent
     * @param type
     * @param packageName
     * @param superClass
     * @param interfaces
     * @param startLine
     * @param endLine
     */
    public void saveComponent(String id, String fileID, String project, String parent, int type, String packageName, String superClass, Collection<String> interfaces, int startLine, int endLine) {
        log.info("Measurement framework: saving component" + id);
        StringJoiner interfaceJoiner = new StringJoiner(",");
        interfaces.forEach(interfaceJoiner::add);

        try (Connection connection = this.dataSource.getConnection()) {
            try (PreparedStatement findComponent = connection.prepareStatement(FIND_COMPONENT)) {
                // start transaction
                connection.setAutoCommit(false);

                findComponent.setString(1, id);
                try (ResultSet component = findComponent.executeQuery()) {
                    if (component.next()) {
                        try (PreparedStatement updateComponent = connection.prepareStatement(UPDATE_COMPONENT)) {
                            updateComponent.setString(1, project);
                            updateComponent.setString(2, fileID);
                            updateComponent.setString(3, parent);
                            updateComponent.setInt(4, type);
                            updateComponent.setString(5, packageName);
                            updateComponent.setString(6, superClass);
                            updateComponent.setString(7, interfaceJoiner.toString());
                            updateComponent.setInt(8, startLine);
                            updateComponent.setInt(9, endLine);
                            updateComponent.setString(10, id);
                            updateComponent.execute();

                        } catch (SQLException e) {
                            log.warn("Can't update the value of component: " + id, e);
                            return;
                        }
                    } else {
                        try (PreparedStatement insertComponent = connection.prepareStatement(INSERT_COMPONENT)) {
                            insertComponent.setString(1, id);
                            insertComponent.setString(2, project);
                            insertComponent.setString(3, fileID);
                            insertComponent.setString(4, parent);
                            insertComponent.setInt(5, type);
                            insertComponent.setString(6, packageName);
                            insertComponent.setString(7, superClass);
                            insertComponent.setString(8, interfaceJoiner.toString());
                            insertComponent.setInt(9, startLine);
                            insertComponent.setInt(10, endLine);
                            insertComponent.execute();

                        } catch (SQLException e) {
                            log.warn("Can't save the value of component: " + id, e);
                            return;
                        }
                    }
                }
                // commit transaction
                connection.commit();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.warn("Can't find component: " + id, e);
            return;
        }
    }

    /**
     * Save measure into DB
     *
     * @param metric
     * @param componentID
     * @param value
     */
    public void saveMeasure(Metric<? extends Serializable> metric, String componentID, int value) {
        try (Connection connection = this.dataSource.getConnection()) {
            try (PreparedStatement saveMeasure = connection.prepareStatement(SAVE_MEASURE)) {
                saveMeasure.setString(1, UUID.randomUUID().toString());
                saveMeasure.setInt(2, value);
                saveMeasure.setString(3, componentID);
                saveMeasure.setString(4, metric.getKey());
                saveMeasure.execute();
            }
        } catch (SQLException e) {
            log.warn("Can't save the measurement for metric: " + metric.getKey(), e);
        }
    }

    /**
     * Get components from components table in embedded DB.
     *
     * @param parent the parent component, for all components it should be <code>null</code>
     * @return collections of components
     */
    public Collection<IComponent> getComponents(String parent) {
        Collection<IComponent> components = new ArrayList<>();

        String selectSql = parent == null ? SELECT_ALL_COMPONENTS : SELECT_COMPONENTS_BY_PARENT;
        try (Connection connection = this.dataSource.getConnection()) {
            try (PreparedStatement selectComponents = connection.prepareStatement(selectSql)) {
                if (parent != null) {
                    selectComponents.setString(1, parent);
                }
                try (ResultSet queryResult = selectComponents.executeQuery()) {
                    while (queryResult.next()) {
                        IComponent component = parseComponentFromQuery(queryResult);
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
        Map<String, Integer> measures = getRecentMeasures(id);
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
    private Map<String, Integer> getRecentMeasures(String id) {
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
     * Get measures for given component
     *
     * @param metric if of the component
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
}
