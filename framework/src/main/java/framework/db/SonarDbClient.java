/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.db;

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
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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
			st.executeUpdate("CREATE TABLE IF NOT EXISTS Measurement_Framework_Components (id varchar(255) NOT NULL, projectKey varchar(255) NOT NULL, fileKey varchar(255) NOT NULL, parent varchar(255), type int NOT NULL,  package varchar(255) NOT NULL, superClass varchar(255) NOT NULL, interfaces varchar(65536) NOT NULL, startLine int, endLine int, PRIMARY KEY (id)); "
					//			st.executeUpdate("CREATE TABLE IF NOT EXISTS Components (id varchar(255) NOT NULL, ProjectsKEE varchar(255) NOT NULL REFERENCES Projects (KEE), startLine int, endLine int, PRIMARY KEY (id)); "
					+ "CREATE TABLE IF NOT EXISTS Measurement_Framework_Measures (id int GENERATED BY DEFAULT AS IDENTITY, value int, Componentsid varchar(255) NOT NULL  REFERENCES Measurement_Framework_Components (id), Metricsid varchar(255) NOT NULL  REFERENCES Metrics (name), PRIMARY KEY (id)); "
					+ "CREATE TABLE IF NOT EXISTS Measurement_Framework_RecentMeasures (id int GENERATED BY DEFAULT AS IDENTITY, value int, Componentsid varchar(255) NOT NULL  REFERENCES Measurement_Framework_Components (id), Metricsid varchar(255) NOT NULL  REFERENCES Metrics (name), PRIMARY KEY (id));");
			st.close();
		} catch (SQLException e) {
			log.warn("Can't create the plugin tables", e);
			return false;
		} 
		return true;
	}

	/**
	 * Create mandatory tables in Sonar DB if they were not created yet.
	 * @return <code>true</code> if tables was created or the were created before, <code>false</code> otherwise
	 */
	public boolean storeMeasures() {
		// check connection
		if (!isConnectionValid()) {
			return false;
		}
		try (Statement st = connection.createStatement()) {
			st.executeUpdate("INSERT INTO Measurement_Framework_Measures (value , Componentsid, Metricsid) SELECT value , Componentsid, Metricsid FROM Measurement_Framework_RecentMeasures; "
					+ "DROP TABLE  Measurement_Framework_RecentMeasures; "
					+ "CREATE TABLE IF NOT EXISTS Measurement_Framework_RecentMeasures (id int GENERATED BY DEFAULT AS IDENTITY, value int, Componentsid varchar(255) NOT NULL REFERENCES Measurement_Framework_Components (id), Metricsid varchar(255) NOT NULL  REFERENCES Metrics (name), PRIMARY KEY (id));");
			st.close();
		} catch (SQLException e) {
			log.warn("Can't save recent measures", e);
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
			st.executeUpdate("DROP TABLE Measurement_Framework_Components; "
					+ "DROP TABLE  Measurement_Framework_Measures; "
					+ "DROP TABLE Measurement_Framework_RecentMeasures");
			st.close();
		} catch (SQLException e) {
			log.warn("Can't drop tables", e);
		} 
		return;
	}

	/**
	 * Save component into DB
	 * @param id
	 * @param fileID 
	 * @param project TODO
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
		log.info("FRAMEWOR: saving component" + id);
		StringJoiner iterfaceJoiner = new StringJoiner(",");
		interfaces.forEach(x -> iterfaceJoiner.add(x));
		try (Statement st = connection.createStatement()) { 
			st.executeUpdate(String.format("MERGE INTO Measurement_Framework_Components (id , projectKey, fileKey, parent, type, package, superClass, interfaces, startLine, endLine) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %s, %s)", 
					id, project, fileID, parent, type, packageName, superClass, iterfaceJoiner.toString(), startLine, endLine));
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
			st.executeUpdate(String.format("INSERT INTO Measurement_Framework_RecentMeasures (value , Componentsid, Metricsid) VALUES (%s, '%s', '%s')", value, componentID, metric.getKey()));
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
			ResultSet queryResult = st.executeQuery(String.format("SELECT * FROM Measurement_Framework_RecentMeasures WHERE Componentsid = '%s' ", id));
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
				measures.add( Integer.valueOf(value));
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
