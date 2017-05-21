/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.db;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.measures.Metric;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Client to access the Sonarqube database and save metrics to it
 *
 * @author Tomas Lestyan
 * @author Klara Erlebachova
 */
public class SaveMetricsClient {

   /** The logger object */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private HikariDataSource dataSource;

    private static final String FIND_COMPONENT = "SELECT * FROM Measurement_Framework_Components WHERE id = ?;";
    private static final String INSERT_COMPONENT = "INSERT INTO Measurement_Framework_Components (id, projectKey, fileKey, parent, type, package, fullyQualifiedName,  superClass, interfaces, isInterface, startLine, endLine) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String UPDATE_COMPONENT = "UPDATE Measurement_Framework_Components SET projectKey = ?, fileKey = ?, parent = ?, type = ?, package = ?, fullyQualifiedName = ?, superClass = ?, interfaces = ?, isInterface = ?, startLine = ?, endLine = ? WHERE id = ?;";


    private static final String COPY_RECENT_MEASURES_TO_MEASURES = "INSERT INTO Measurement_Framework_Measures (id, value , Componentsid, Metricsid) " +
            "SELECT id, value , Componentsid, Metricsid FROM Measurement_Framework_Recent_Measures " +
            "WHERE id NOT IN (SELECT id FROM Measurement_Framework_Measures);";

    private static final String DELETE_RECENT_MEASURE = "DELETE FROM Measurement_Framework_Recent_Measures WHERE Componentsid = ? and Metricsid = ?";
    private static final String SAVE_RECENT_MEASURE = "INSERT INTO Measurement_Framework_Recent_Measures (id, value , Componentsid, Metricsid) VALUES (?, ?, ?, ?)";

    /**
     * Constructor
     */
    public SaveMetricsClient(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * Copy recent measures to measures
     *
     * @return <code>true</code> if tables was created or the were created before, <code>false</code> otherwise
     */
    public boolean saveRecentMeasuresToMeasures() {
        try (Connection connection = this.dataSource.getConnection()) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(COPY_RECENT_MEASURES_TO_MEASURES);
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
    public void saveComponent(String id, String fileID, String project, String parent, int type, String packageName, String fqName, String superClass, Collection<String> interfaces, boolean isInterface, int startLine, int endLine) {
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
                            updateComponent.setString(6, fqName);
                            updateComponent.setString(7, superClass);
                            updateComponent.setString(8, interfaceJoiner.toString());
                            updateComponent.setBoolean(9, isInterface);
                            updateComponent.setInt(10, startLine);
                            updateComponent.setInt(11, endLine);
                            updateComponent.setString(12, id);
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
                            insertComponent.setString(7, fqName);
                            insertComponent.setString(8, superClass);
                            insertComponent.setString(9, interfaceJoiner.toString());
                            insertComponent.setBoolean(10, isInterface);
                            insertComponent.setInt(11, startLine);
                            insertComponent.setInt(12, endLine);
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
            // start transaction
            connection.setAutoCommit(false);

            try (PreparedStatement deleteMeasure = connection.prepareStatement(DELETE_RECENT_MEASURE)) {
                deleteMeasure.setString(1, componentID);
                deleteMeasure.setString(2, metric.getKey());
                deleteMeasure.execute();
            }

            try (PreparedStatement saveMeasure = connection.prepareStatement(SAVE_RECENT_MEASURE)) {
                saveMeasure.setString(1, UUID.randomUUID().toString());
                saveMeasure.setInt(2, value);
                saveMeasure.setString(3, componentID);
                saveMeasure.setString(4, metric.getKey());
                saveMeasure.execute();
            }

            // commit transaction
            connection.commit();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            log.warn("Can't save the measurement for metric: " + metric.getKey(), e);
        }
    }
}
