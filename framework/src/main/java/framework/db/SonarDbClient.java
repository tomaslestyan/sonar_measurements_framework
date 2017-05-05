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
public class SonarDbClient implements IDbClient {

    public static final SonarDbClient INSTANCE = new SonarDbClient();
    /** The logger object */
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /** DB connection */
    private Connection connection;
    /** Timeout for reasonable connection validation  */
    private static final int TIMEOUT = 1000;

    private static final String CREATE_COMPONENTS = "CREATE TABLE IF NOT EXISTS Measurement_Framework_Components (" +
            "id varchar(255) NOT NULL, " +
            "projectKey varchar(255) NOT NULL, " +
            "fileKey varchar(255) NOT NULL, " +
            "parent varchar(255), " +
            "type int NOT NULL,  " +
            "package varchar(255) NOT NULL, " +
            "superClass varchar(255) NOT NULL, " +
            "interfaces varchar(65536) NOT NULL, " +
            "startLine int, " +
            "endLine int, " +
            "PRIMARY KEY (id));";

    private static final String MEASURES_COLUMNS =
            "(id varchar(255) NOT NULL, " +
                    "value int, " +
                    "ComponentsId varchar(255) NOT NULL REFERENCES Measurement_Framework_Components (id), " +
                    "MetricsId varchar(255) NOT NULL REFERENCES Metrics (name), PRIMARY KEY (id));";

    private static final String CREATE_MEASURES = "CREATE TABLE IF NOT EXISTS Measurement_Framework_Measures " + MEASURES_COLUMNS;
    private static final String CREATE_RECENT_MEASURES = "CREATE TABLE IF NOT EXISTS Measurement_Framework_Recent_Measures " + MEASURES_COLUMNS;

    private static final String DELETE_COMPONENT = "DELETE FROM Measurement_Framework_Components WHERE id = ?";
    private static final String INSERT_COMPONENT = "INTO Measurement_Framework_Components (id , projectKey, fileKey, parent, type, package, superClass, interfaces, startLine, endLine) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    /**
     * Constructor
     */
    private SonarDbClient() {
        connect();
    }

    /**
     * (Try to) Establish DB connection
     * Do not forget to call disconnect() after session ends.
     */
    private void connect() {
        try {
            if (!isConnected()) {
                Configuration configuration = Configuration.INSTANCE;

                if (!configuration.verifyJdbcDriver()) {
                    log.warn("JDBC driver cannot be found");
                }
                connection = DriverManager.getConnection(
                        configuration.getConnectionString(),
                        configuration.getDbUser(),
                        configuration.getDbPassword()
                );
            }
        } catch (SQLException e) {
            // could not create connection
            connection = null;
            log.warn("Connection with SonarQube database not established", e);
        }
    }

    /**
     * Disconnect from DB. It has to be used to terminate connection.
     */
    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.warn("Can't close the connection", e);
        }
    }

    /**
     * @return <code>true</code> if client is connected, <code>false</code> otherwise
     */
    public boolean isConnected() {
        return isConnectionValid();
    }


    /**
     * Create mandatory tables in Sonar DB if they were not created yet.
     *
     * @return <code>true</code> if tables was created or the were created before, <code>false</code> otherwise
     */
    public boolean createTables() {
        // check connection
        if (!isConnectionValid()) {
            return false;
        }
        try (Statement st = this.connection.createStatement()) {
            st.executeUpdate(CREATE_COMPONENTS + CREATE_MEASURES + CREATE_RECENT_MEASURES);
            st.close();
        } catch (SQLException e) {
            log.warn("Can't create the plugin tables", e);
            return false;
        }
        return true;
    }

    /**
     * Create mandatory tables in Sonar DB if they were not created yet.
     *
     * @return <code>true</code> if tables was created or the were created before, <code>false</code> otherwise
     */
    public boolean saveRecentMeasuresToMeasures() {
        // check connection
        if (!isConnectionValid()) {
            return false;
        }
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "INSERT INTO Measurement_Framework_Measures (id, value , Componentsid, Metricsid) " +
                    "SELECT id, value , Componentsid, Metricsid FROM Measurement_Framework_Recent_Measures " +
                    "WHERE id NOT IN (SELECT id FROM Measurement_Framework_Measures);" +
                    "DELETE FROM Measurement_Framework_Recent_Measures; ");
            st.close();
        } catch (SQLException e) {
            log.warn("Can't save recent measures to measures", e);
            return false;
        }
        return true;
    }

    /**
     * Drop mandatory tables in Sonar DB
     */
    public void dropTables() {
        // check connection
        if (!isConnectionValid()) {
            return;
        }
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "DROP TABLE Measurement_Framework_Components; " +
                    "DROP TABLE  Measurement_Framework_Measures; " +
                    "DROP TABLE Measurement_Framework_Recent_Measures")
            ;
            st.close();
        } catch (SQLException e) {
            log.warn("Can't drop tables", e);
        }
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
        // check connection
        if (!isConnectionValid()) {
            return;
        }
        log.info("Measurement framework: saving component" + id);
        StringJoiner interfaceJoiner = new StringJoiner(",");
        interfaces.forEach(interfaceJoiner::add);

        try {
            // start transaction
            connection.setAutoCommit(false);

            try (PreparedStatement deleteComponent = connection.prepareStatement(DELETE_COMPONENT)) {
                deleteComponent.setString(1, id);
                deleteComponent.execute();
            } catch (SQLException e) {
                log.warn("Can't delete component: " + id, e);
            }

            try (PreparedStatement insertComponent = connection.prepareStatement(INSERT_COMPONENT))
            {
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
            }

            // commit transaction
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            log.warn("Can't proceed transaction" + id, e);
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
        // check connection
        if (!isConnectionValid()) {
            return;
        }

        String saveMeasureSql= "INSERT INTO Measurement_Framework_Recent_Measures (id, value , Componentsid, Metricsid) VALUES (?, ?, ?, ?)";

        try (PreparedStatement saveMeasure = connection.prepareStatement(saveMeasureSql)) {
            saveMeasure.setString(1, UUID.randomUUID().toString());
            saveMeasure.setInt(2, value);
            saveMeasure.setString(3, componentID);
            saveMeasure.setString(4, metric.getKey());
            saveMeasure.execute();
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
        // check connection
        if (!isConnectionValid()) {
            return Collections.emptyList();
        }
        Collection<IComponent> components = new ArrayList<>();
        try (Statement st = connection.createStatement()) {
            ResultSet queryResult = (parent == null)
                    ? st.executeQuery("SELECT * FROM Measurement_Framework_Components")
                    : st.executeQuery(String.format("SELECT * FROM Measurement_Framework_Components WHERE parent = '%s' ", parent));
            while (queryResult.next()) {
                IComponent component = parseComponentFromQuery(queryResult);
                if (component != null) {
                    components.add(component);
                }
            }
            queryResult.close();
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
        // check connection
        if (!isConnectionValid()) {
            return null;
        }
        try (Statement st = connection.createStatement()) {
            Map<String, Integer> measures = new HashMap<>();
            ResultSet queryResult = st.executeQuery(String.format("SELECT * FROM Measurement_Framework_Recent_Measures WHERE Componentsid = '%s' ", id));
            while (queryResult.next()) {
                String metric = queryResult.getString("Metricsid");
                int value = queryResult.getInt("value");
                measures.put(metric, Integer.valueOf(value));
            }
            queryResult.close();
            return measures;
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
        // check connection
        if (!isConnectionValid()) {
            return null;
        }
        try (Statement st = connection.createStatement()) {
            List<Integer> measures = new ArrayList<>();
            ResultSet queryResult = st.executeQuery(String.format("SELECT * FROM Measurement_Framework_Measures WHERE Metricsid = '%s' ", metric));
            while (queryResult.next()) {
                int value = queryResult.getInt("value");
                measures.add(Integer.valueOf(value));
            }
            queryResult.close();
            return measures;
        } catch (SQLException e) {
            log.warn("Can't retrieve measures", e);
        }
        return null;
    }

    /**
     * Check if connection is valid
     *
     * @return <code>true</code> if is valid, <code>false</code> otherwise
     * @throws SQLException
     */
    private boolean isConnectionValid() {
        try {
            return connection != null && connection.isValid(TIMEOUT);
        } catch (SQLException e) {
            log.warn("Can't validate connection", e);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     * Do not rely on this finalize. Yes it disconnects from DB, but not guaranteed when the GC will call it.
     */
    @Override
    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }
}
