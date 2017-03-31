/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.measures.Metric;

import main.java.components.ClassComponent;
import main.java.components.IComponent;
import main.java.components.MethodComponent;

/**
 * Client to access the SonarQube H2 database
 * @author Tomas Lestyan
 */
public class SonarDbClient {

	/** The logger object */
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/** Timeout for reasonable connection validation */
	private static final int DEFAULT_TIMEOUT = 1000;
	/** URL of  embedded SonarQube DB */
	private static final String DB_URL = "jdbc:h2:tcp://localhost:9092/sonar";
	/** H2 driver used */
	private static final String JDBC_H2_DRIVER = "org.h2.Driver";
	/** DB connection */
	private Connection connection;

	/**
	 * Constructor
	 * @param connect <code>true</code> for establish connection with DB, false when connection will be established later. Use connect() method in that case.
	 */
	public SonarDbClient(boolean connect) {
		if (connect) {
			connect();
		}
	}

	/**
	 * (Try to) Establish DB connection
	 * Do not forget to call disconnect() after session ends.
	 */
	public void connect() {
		try {
			if (!isConnected()) {				
				Class.forName(JDBC_H2_DRIVER);
				connection = DriverManager.getConnection(DB_URL);
			}
		} catch (ClassNotFoundException | SQLException e) {
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
	 * @return <code>true</code> if tables was created or the were created before, <code>false</code> otherwise
	 */
	public boolean createTables() {
		// check connection
		if (!isConnectionValid()) {
			return false;
		}
		try (Statement st = connection.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS Components (id varchar(255) NOT NULL, ProjectsKEE varchar(255) NOT NULL, parent varchar(255), type varchar(255) NOT NULL, startLine int, endLine int, PRIMARY KEY (id)); "
					//			st.executeUpdate("CREATE TABLE IF NOT EXISTS Components (id varchar(255) NOT NULL, ProjectsKEE varchar(255) NOT NULL REFERENCES Projects (KEE), startLine int, endLine int, PRIMARY KEY (id)); "
					+ "CREATE TABLE IF NOT EXISTS Measures (id int GENERATED BY DEFAULT AS IDENTITY, Metricsid varchar(255) NOT NULL  REFERENCES Metrics (id), Componentsid varchar(255) NOT NULL  REFERENCES Metrics (name), value int, PRIMARY KEY (id)); "
					+ "CREATE TABLE IF NOT EXISTS RecentMeasures (id int GENERATED BY DEFAULT AS IDENTITY, value int, Componentsid varchar(255) NOT NULL  REFERENCES Components (id), Metricsid varchar(255) NOT NULL  REFERENCES Metrics (name), PRIMARY KEY (id));");
			st.close();
		} catch (SQLException e) {
			log.warn("Can't create the metrics and records table", e);
			return false;
		} 
		return true;
	}

	/**
	 * Save component into DB
	 * @param id
	 * @param sonarComponentID
	 * @param parent
	 * @param type
	 * @param startLine
	 * @param endLine
	 */
	public void saveComponent(String id, String sonarComponentID, String parent, String type, int startLine, int endLine) {
		// check connection
		if (!isConnectionValid()) {
			return;
		}
		try (Statement st = connection.createStatement()) { 
			st.executeUpdate(String.format("MERGE INTO Components (id , ProjectsKEE, parent, type, startLine, endLine) VALUES ('%s', '%s', '%s', '%s', %s, %s)", id, sonarComponentID, parent, type, startLine, endLine));
		} catch (SQLException e) {
			log.warn("Can't save the value of component: " + id, e);
		}
	}

	/**
	 * Save measure into DB
	 * @param metric
	 * @param componentID
	 * @param value
	 */
	public void saveMeasure(Metric<? extends Serializable> metric, String componentID, int value) {
		// check connection
		if (!isConnectionValid()) {
			return;
		}
		try (Statement st = connection.createStatement()) {
			st.executeUpdate(String.format("INSERT INTO RecentMeasures (value , Componentsid, Metricsid) VALUES (%s, '%s', '%s')", value, componentID, metric.getName()));
		} catch (SQLException e) {
			log.warn("Can't save the measurement for metric: " + metric.getKey(), e);
		}
	}

	/** Get components from components table in embedded DB.
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
					? st.executeQuery("SELECT * FROM Components") 
							: st.executeQuery(String.format("SELECT * FROM Components WHERE parent = '%s' ", parent)); 
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
	 * @param queryResult
	 * @throws SQLException
	 */
	private IComponent parseComponentFromQuery(ResultSet queryResult) throws SQLException {
		String id = queryResult.getString("ID");
		String sonarKey = queryResult.getString("PROJECTSKEE");
		String parentID = queryResult.getString("PARENT");
		int start = queryResult.getInt("STARTLINE");
		int end = queryResult.getInt("ENDLINE");
		Map<Object, Object> measures = getMeasures(id);
		if (queryResult.getString("TYPE").equalsIgnoreCase("method")) {
			return new MethodComponent(id, sonarKey, parentID, measures);
		} else if (queryResult.getString("TYPE").equalsIgnoreCase("class")) {
			return new ClassComponent(id, sonarKey, parentID, getComponents(id), measures);
		}
		log.warn("Incorrect component type of component with ID = " + id);
		return null;
	}

	/**
	 * Get measures for given component
	 * @param id if of the component
	 * @return measures
	 */
	private Map<Object, Object> getMeasures(String id) {
		// check connection
		if (!isConnectionValid()) {
			return null;
		}
		try (Statement st = connection.createStatement()) {
			Map<Object, Object> measures = new HashMap<>();
			ResultSet queryResult = st.executeQuery(String.format("SELECT * FROM RecentMeasures WHERE Componentsid = '%s' ", id));
			while (queryResult.next()) {
				String metric = queryResult.getString("Metricsid");
				int value = queryResult.getInt("value");
				measures.put(metric, Integer.valueOf(value));
			}
			queryResult.close();
			return measures;
		} catch (SQLException e) {
			log.warn("Can't retrieve components", e);
		}
		return null;
	}

	/**
	 * Check if connection is valid
	 * @return <code>true</code> if is valid, <code>false</code> otherwise
	 * @throws SQLException
	 */
	private boolean isConnectionValid()  {
		try {
			return connection != null && connection.isValid(DEFAULT_TIMEOUT);
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
